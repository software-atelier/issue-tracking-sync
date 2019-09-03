package ch.loewenfels.issuetrackingsync.syncclient.jira

import ch.loewenfels.issuetrackingsync.*
import ch.loewenfels.issuetrackingsync.syncclient.IssueClientException
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.io.IOUtils
import org.joda.time.DateTime
import org.springframework.beans.BeanWrapperImpl
import java.io.ByteArrayInputStream
import java.net.URI
import java.time.*
import java.time.format.DateTimeFormatter

/**
 * JIRA Java client, see (https://ecosystem.atlassian.net/wiki/spaces/JRJC/overview)
 */
open class JiraClient(private val setup: IssueTrackingApplication) :
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
        val jql = if (fieldName.startsWith("customfield")) {
            val cfNumber = fieldName.substring(12)
            "cf[$cfNumber] ~ '$fieldValue'"
        } else {
            "$fieldName = '$fieldValue'"
        }
        val foundIssues = jiraRestClient.searchClient.searchJql(jql).claim().issues.toList()
        return when (foundIssues.size) {
            0 -> null
            // reload to get full issue incl. collections such as comments
            1 -> getProprietaryIssue(foundIssues[0].key)
            else -> throw IssueClientException("Query too broad, multiple issues found for $fieldValue")
        }
    }

    override fun getIssue(key: String): Issue? {
        return mapJiraIssue(getJiraIssue(key))
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

    override fun getKey(internalIssue: com.atlassian.jira.rest.client.api.domain.Issue): String =
        internalIssue.key

    override fun getIssueUrl(internalIssue: com.atlassian.jira.rest.client.api.domain.Issue): String =
        "${setup.endpoint}/browse/${internalIssue.key}".replace("//", "/")

    override fun getLastUpdated(internalIssue: com.atlassian.jira.rest.client.api.domain.Issue): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(internalIssue.updateDate.millis), ZoneId.systemDefault())

    override fun getValue(internalIssue: com.atlassian.jira.rest.client.api.domain.Issue, fieldName: String): Any? {
        val beanWrapper = BeanWrapperImpl(internalIssue)
        val internalValue = if (beanWrapper.isReadableProperty(fieldName))
            beanWrapper.getPropertyValue(fieldName)
        else
            null
        return internalValue?.let { convertFromMetadataId(fieldName, it) }
    }

    override fun setValue(
        internalIssueBuilder: Any,
        issue: Issue,
        fieldName: String,
        value: Any?
    ) {
        convertToMetadataId(fieldName, value)?.let {
            val beanWrapper = BeanWrapperImpl(internalIssueBuilder)
            if (beanWrapper.isWritableProperty(fieldName))
                beanWrapper.setPropertyValue(fieldName, it)
            else if (internalIssueBuilder is IssueInputBuilder) {
                val targetInternalIssue = (issue.proprietaryTargetInstance
                    ?: throw IllegalStateException("Need a target issue for custom fields")) as com.atlassian.jira.rest.client.api.domain.Issue

                setInternalFieldValue(internalIssueBuilder, targetInternalIssue, fieldName, it)
            }
        }
    }

    private fun convertToMetadataId(fieldName: String, value: Any?): Any? {
        return when (fieldName) {
            "priorityId" -> JiraMetadata.getPriorityId(value?.toString() ?: "", jiraRestClient)
            "issueTypeId" -> JiraMetadata.getIssueTypeId(value?.toString() ?: "", jiraRestClient)
            else -> value
        }
    }

    private fun convertFromMetadataId(fieldName: String, value: Any): Any {
        return when (fieldName) {
            "priorityId" -> JiraMetadata.getPriorityName(value.toString().toLong(), jiraRestClient)
            else -> value
        }
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
        when {
            targetIssue != null -> updateTargetIssue(targetIssue, issue)
            defaultsForNewIssue != null -> createTargetIssue(defaultsForNewIssue, issue)
            else -> throw SynchronizationAbortedException("No target issue found for $targetIssueKey, and no defaults for creating issue were provided")
        }
    }

    private fun createTargetIssue(
        defaultsForNewIssue: DefaultsForNewIssue,
        issue: Issue
    ) {
        val issueType = JiraMetadata.getIssueTypeId(defaultsForNewIssue.issueType, jiraRestClient)
        val issueBuilder = IssueInputBuilder()
            .setIssueTypeId(issueType)
            .setProjectKey(defaultsForNewIssue.project)
        //  here we need to delay custom fields until issue has been created!
        val beanWrapper = BeanWrapperImpl(issueBuilder)
        issue.fieldMappings.filter { beanWrapper.isWritableProperty(it.targetName) }
            .forEach {
                it.setTargetValue(issueBuilder, issue, this)
            }
        val basicIssue = jiraRestClient.issueClient.createIssue(issueBuilder.build()).claim()
        logger().info("Created new JIRA issue ${basicIssue.key}")
        val targetIssue =
            getProprietaryIssue(basicIssue.key) ?: throw IssueClientException("Failed to locate newly created issue")
        updateTargetIssue(targetIssue, issue)
    }

    private fun updateTargetIssue(targetIssue: com.atlassian.jira.rest.client.api.domain.Issue, issue: Issue) {
        issue.proprietaryTargetInstance = targetIssue
        val issueBuilder = IssueInputBuilder()
        issue.fieldMappings.forEach {
            it.setTargetValue(issueBuilder, issue, this)
        }
        logger().info("Updating JIRA issue ${targetIssue.key}")
        jiraRestClient.issueClient.updateIssue(targetIssue.key, issueBuilder.build()).claim()
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

    override fun getComments(internalIssue: com.atlassian.jira.rest.client.api.domain.Issue): List<Comment> =
        internalIssue.comments.map { jiraComment ->
            Comment(
                jiraComment.author?.displayName ?: "n/a",
                toLocalDateTime(jiraComment.creationDate),
                jiraComment.body
            )
        }

    override fun addComment(internalIssue: com.atlassian.jira.rest.client.api.domain.Issue, comment: Comment) {
        val jiraComment = com.atlassian.jira.rest.client.api.domain.Comment.valueOf(comment.content)
        jiraRestClient.issueClient.addComment(internalIssue.commentsUri, jiraComment).claim()
    }

    override fun getAttachments(internalIssue: com.atlassian.jira.rest.client.api.domain.Issue): List<Attachment> =
        internalIssue.attachments?.map { jiraAttachment ->
            val attachmentInputStreamPromise = jiraRestClient.issueClient.getAttachment(jiraAttachment.contentUri)
            attachmentInputStreamPromise.get().use {
                Attachment(
                    jiraAttachment.filename,
                    IOUtils.toByteArray(it)
                )
            }
        } ?: listOf()

    override fun addAttachment(internalIssue: com.atlassian.jira.rest.client.api.domain.Issue, attachment: Attachment) {
        jiraRestClient.issueClient.addAttachment(
            internalIssue.attachmentsUri,
            ByteArrayInputStream(attachment.content),
            attachment.filename
        ).claim()
    }

    fun verifySetup(): String {
        return jiraRestClient.metadataClient.serverInfo.claim().serverTitle
    }

    fun listFields() {
        jiraRestClient.metadataClient.fields.claim().forEach { f ->
            println("${f.id} / ${f.name} type ${f.fieldType} ${f.schema?.type}")
        }
    }

    private fun mapJiraIssue(jiraIssue: com.atlassian.jira.rest.client.api.domain.Issue): Issue {
        return Issue(
            jiraIssue.key,
            setup.name,
            getLastUpdated(jiraIssue)
        )
    }

    /**
     * More generic method to set values where a simple setter on the [internalIssueBuilder] is not available.
     *
     * (https://developer.atlassian.com/server/jira/platform/rest-apis/) gives some more insight
     */
    private fun setInternalFieldValue(
        internalIssueBuilder: IssueInputBuilder,
        jiraIssue: com.atlassian.jira.rest.client.api.domain.Issue,
        fieldName: String,
        value: Any
    ) {
        val fld = jiraIssue.getFieldByName(fieldName) ?: jiraIssue.getField(fieldName)
        ?: throw IllegalArgumentException("Unknown field $fieldName")
        // you might be tempted to query [metadataClient] directly here. However, JIRA setup allows to map fields
        // to certain projects only, and so finding a field in the metadataClient does NOT mean it is available
        // in the project the issue is assigned to
        val fldType = JiraMetadata.getFieldType(fld.id, jiraRestClient)
        when (fldType) {
            // Text custom field
            "string" -> internalIssueBuilder.setFieldValue(fld.id, value.toString())
            "option" -> {
                val complexValue = ComplexIssueInputFieldValue.with("value", value.toString())
                internalIssueBuilder.setFieldValue(fld.id, complexValue)
            }
        }
    }

    private fun toLocalDateTime(jodaDateTime: DateTime): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(jodaDateTime.toInstant().millis), ZoneId.systemDefault())
}
