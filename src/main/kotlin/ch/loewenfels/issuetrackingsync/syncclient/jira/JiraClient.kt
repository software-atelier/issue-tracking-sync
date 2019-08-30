package ch.loewenfels.issuetrackingsync.syncclient.jira

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.Logging
import ch.loewenfels.issuetrackingsync.SynchronizationAbortedException
import ch.loewenfels.issuetrackingsync.logger
import ch.loewenfels.issuetrackingsync.syncclient.IssueClientException
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.BeanWrapperImpl
import java.net.URI
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class JiraClient(private val setup: IssueTrackingApplication) :
    IssueTrackingClient<com.atlassian.jira.rest.client.api.domain.Issue>, Logging {
    private val jiraRestClient: JiraRestClient = AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(
        URI(setup.endpoint),
        setup.username,
        setup.password
    )

    override fun getProprietaryIssue(issueKey: String): com.atlassian.jira.rest.client.api.domain.Issue? {
        return getJiraIssue(issueKey)
    }

    override fun getProprietaryIssue(
        fieldName: String,
        fieldValue: String
    ): com.atlassian.jira.rest.client.api.domain.Issue? {
        val jql = if (fieldName.startsWith("custom_field")) {
            val cfNumber = fieldName.substring(12)
            "cf[$cfNumber] ~ '$fieldValue'"
        } else {
            "$fieldName = '$fieldValue'"
        }
        val foundIssues = jiraRestClient.searchClient.searchJql(jql).claim().issues.toList()
        return when (foundIssues.size) {
            0 -> null
            1 -> foundIssues[0]
            else -> throw IssueClientException("Query too broad, multiple issues found for $fieldValue")
        }
    }

    override fun getIssue(key: String): Issue? {
        return getJiraIssue(key)
            .let { mapJiraIssue(it) }
    }

    override fun getIssueFromWebhookBody(body: JsonNode): Issue {
        val formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][xxx][xx][X]")
        return Issue(
            body.get("issue")?.get("key")?.asText() ?: "",
            setup.name,
            OffsetDateTime.parse(
                body.get("issue")?.get("fields")?.get("updated")?.asText() ?: "",
                formatter
            ).toLocalDateTime()
        )
    }

    override fun getLastUpdated(internalIssue: com.atlassian.jira.rest.client.api.domain.Issue): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(internalIssue.updateDate.millis), ZoneId.systemDefault())

    override fun getValue(internalIssue: com.atlassian.jira.rest.client.api.domain.Issue, fieldName: String): Any? {
        return BeanWrapperImpl(internalIssue).getPropertyValue(fieldName)
    }

    override fun setValue(internalIssueBuilder: Any, fieldName: String, value: Any?) {
        value?.let { BeanWrapperImpl(internalIssueBuilder).setPropertyValue(fieldName, it) }
    }

    private fun getJiraIssue(key: String): com.atlassian.jira.rest.client.api.domain.Issue {
        return jiraRestClient.issueClient.getIssue(key).claim()
    }

    override fun createOrUpdateTargetIssue(
        issue: Issue,
        defaultsForNewIssue: DefaultsForNewIssue?
    ) {
        val targetKeyFieldname = issue.keyFieldMapping!!.getTargetFieldname()
        val targetIssueKey = issue.keyFieldMapping!!.getKeyForTargetIssue().toString()
        val targetIssue =
            if (targetIssueKey.isNotEmpty()) getProprietaryIssue(targetKeyFieldname, targetIssueKey) else null
        if (targetIssue != null) {
            updateTargetIssue(targetIssue, issue)
        } else if (defaultsForNewIssue != null) {
            createTargetIssue(defaultsForNewIssue, issue)
        } else {
            throw SynchronizationAbortedException("No target issue found for $targetIssueKey, and no defaults for creating issue were provided")
        }
    }

    private fun createTargetIssue(defaultsForNewIssue: DefaultsForNewIssue, issue: Issue) {
        val issueBuilder = IssueInputBuilder()
        issueBuilder.setIssueTypeId(defaultsForNewIssue.issueType.toLong())
        issueBuilder.setProjectKey(defaultsForNewIssue.project)
        issue.fieldMappings.forEach {
            it.setTargetValue(issueBuilder, this)
        }
        // TODO: enable val basicIssue = jiraRestClient.issueClient.createIssue(issueBuilder.build()).claim()
        // logger().info("Created new JIRA issue ${basicIssue.key}")
        // TODO: update collections such as comments and attachments
    }

    private fun updateTargetIssue(targetIssue: com.atlassian.jira.rest.client.api.domain.Issue, issue: Issue) {
        val issueBuilder = IssueInputBuilder()
        issue.fieldMappings.forEach {
            it.setTargetValue(issueBuilder, this)
        }
        logger().info("Updating JIRA issue ${targetIssue.key}")
        // TODO: enable jiraRestClient.issueClient.updateIssue(targetIssue.key, issueBuilder.build()).claim()
    }

    override fun changedIssuesSince(lastPollingTimestamp: LocalDateTime): Collection<Issue> {
        val lastPollingTimestampAsString =
            lastPollingTimestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        var jql = "(updated >= '$lastPollingTimestampAsString' OR created >= '$lastPollingTimestampAsString')"
        setup.project.let { jql += " AND project = '$it'" }
        return jiraRestClient.searchClient
            .searchJql("$jql ORDER BY key")
            .claim()
            .issues
            .map { mapJiraIssue(it) }
            .toList()
    }

    fun verifySetup(): String {
        return jiraRestClient.metadataClient.serverInfo.claim().serverTitle
    }

    private fun mapJiraIssue(jiraIssue: com.atlassian.jira.rest.client.api.domain.Issue): Issue {
        return Issue(
            jiraIssue.key,
            setup.name,
            getLastUpdated(jiraIssue)
        )
    }
}
