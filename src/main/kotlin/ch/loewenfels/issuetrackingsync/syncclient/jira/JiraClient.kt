package ch.loewenfels.issuetrackingsync.syncclient.jira

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.ApplicationRole
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import java.net.URI
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class JiraClient(private val setup: IssueTrackingApplication) : IssueTrackingClient {
    private val jiraRestClient: JiraRestClient = AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(
        URI(setup.endpoint),
        setup.username,
        setup.password
    )

    override fun getIssue(key: String): Issue? {
        return jiraRestClient.issueClient.getIssue(key).claim()
            .let { mapJiraIssue(it) }
    }

    override fun changedIssuesSince(lastPollingTimestamp: LocalDateTime): Collection<Issue> {
        val lastPollingTimestampAsString = lastPollingTimestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val hasPartnerApplicationLink =
            setup.fieldsHoldingPartnerApplicationKey.map { "cf[${it.value}] IS NOT EMPTY" }.joinToString(" OR ")
        val createdCutoff = lastPollingTimestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        var jql = "(updated >= '$lastPollingTimestampAsString'"
        if (hasPartnerApplicationLink.isNotEmpty()) {
            jql += " AND ($hasPartnerApplicationLink)"
        }
        jql += ")"
        if (setup.role != ApplicationRole.SLAVE) {
            jql += " OR (created >= '$lastPollingTimestampAsString')"
        }
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
            LocalDateTime.ofInstant(Instant.ofEpochMilli(jiraIssue.updateDate.millis), ZoneId.systemDefault())
        )
    }
}
