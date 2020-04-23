package ch.loewenfels.issuetrackingsync.syncclient.jira

import ch.loewenfels.issuetrackingsync.Attachment
import ch.loewenfels.issuetrackingsync.Comment
import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.Logging
import ch.loewenfels.issuetrackingsync.StateHistory
import ch.loewenfels.issuetrackingsync.SynchronizationAbortedException
import ch.loewenfels.issuetrackingsync.executor.SyncActionName
import ch.loewenfels.issuetrackingsync.executor.actions.SynchronizationAction
import ch.loewenfels.issuetrackingsync.logger
import ch.loewenfels.issuetrackingsync.notification.NotificationObserver
import ch.loewenfels.issuetrackingsync.syncclient.IssueClientException
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import com.atlassian.jira.rest.client.api.IssueRestClient
import com.atlassian.jira.rest.client.api.RestClientException
import com.atlassian.jira.rest.client.api.domain.IssueField
import com.atlassian.jira.rest.client.api.domain.IssueFieldId
import com.atlassian.jira.rest.client.api.domain.Resolution
import com.atlassian.jira.rest.client.api.domain.TimeTracking
import com.atlassian.jira.rest.client.api.domain.Transition
import com.atlassian.jira.rest.client.api.domain.Version
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue
import com.atlassian.jira.rest.client.api.domain.input.FieldInput
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput
import com.atlassian.renderer.wysiwyg.converter.DefaultWysiwygConverter
import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.io.IOUtils
import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONObject
import org.joda.time.DateTime
import org.springframework.beans.BeanWrapperImpl
import org.springframework.http.HttpStatus
import java.io.ByteArrayInputStream
import java.net.URI
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Collections
import com.atlassian.jira.rest.client.api.domain.Issue as JiraProprietaryIssue

/**
 * JIRA Java client, see (https://ecosystem.atlassian.net/wiki/spaces/JRJC/overview)
 */
open class JiraClient(private val setup: IssueTrackingApplication) :
    IssueTrackingClient<JiraProprietaryIssue>, Logging {
    private val backReferenceCommentId = "00000000"
    private val jiraRestClient = ExtendedAsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(
        URI(setup.endpoint),
        setup.username,
        setup.password
    )

    override fun getProprietaryIssue(issueKey: String): JiraProprietaryIssue? {
        return getJiraIssue(issueKey)
    }

    override fun getProprietaryIssue(
        fieldName: String,
        fieldValue: String
    ): JiraProprietaryIssue? {
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

    override fun getKey(internalIssue: JiraProprietaryIssue): String =
        internalIssue.key

    override fun getIssueUrl(internalIssue: JiraProprietaryIssue): String {
        val endpoint =
            if (setup.endpoint.endsWith("/")) setup.endpoint.substring(0, setup.endpoint.length - 1) else setup.endpoint
        return "$endpoint/browse/${internalIssue.key}"
    }

    override fun getLastUpdated(internalIssue: JiraProprietaryIssue): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(internalIssue.updateDate.millis), ZoneId.systemDefault())

    override fun getHtmlValue(internalIssue: JiraProprietaryIssue, fieldName: String) =
        jiraRestClient.getHtmlRenderingRestClient().getRenderedHtml(internalIssue.key, fieldName)

    override fun getValue(internalIssue: JiraProprietaryIssue, fieldName: String): Any? {
        val beanWrapper = BeanWrapperImpl(internalIssue)
        val internalValue = if (beanWrapper.isReadableProperty(fieldName))
            beanWrapper.getPropertyValue(fieldName)
        else
            getCustomFields(internalIssue, fieldName)
        return internalValue?.let { convertFromMetadataId(fieldName, it) }
    }

    override fun setValue(
        internalIssueBuilder: Any,
        issue: Issue,
        fieldName: String,
        value: Any?
    ) {
        logger().debug("Setting value $value on $fieldName")
        val proprietaryJiraIssue = issue.proprietaryTargetInstance
        convertToMetadataId(fieldName, value, proprietaryJiraIssue as JiraProprietaryIssue?)?.let {
            val beanWrapper = BeanWrapperImpl(internalIssueBuilder)
            if (beanWrapper.isWritableProperty(fieldName))
                beanWrapper.setPropertyValue(fieldName, it)
            else if (internalIssueBuilder is IssueInputBuilder) {
                val targetInternalIssue = (proprietaryJiraIssue
                    ?: throw IllegalStateException("Need a target issue for custom fields"))
                if (fieldName == "timeTracking" && value is TimeTracking) {
                    setInternalFieldValue(internalIssueBuilder, IssueFieldId.TIMETRACKING_FIELD.id, value)
                } else if (fieldName == "labels" && value is List<*>) {
                    setInternalFieldValue(internalIssueBuilder, IssueFieldId.LABELS_FIELD.id, value)
                } else if (fieldName == "versions") {
                    // RTC allows only one version (field: foundIn) while Jira awaits a list of versions
                    setInternalFieldValue(
                        internalIssueBuilder,
                        IssueFieldId.AFFECTS_VERSIONS_FIELD.id,
                        mutableListOf(value)
                    )
                } else if (fieldName == "resolution" && value is String) {
                    setResolution(targetInternalIssue, value)
                } else {
                    setInternalFieldValue(internalIssueBuilder, targetInternalIssue, fieldName, it)
                }
            }
        }
    }

    override fun setHtmlValue(internalIssueBuilder: Any, issue: Issue, fieldName: String, htmlString: String) {
        val convertedValue = DefaultWysiwygConverter().convertXHtmlToWikiMarkup(htmlString)
        setValue(internalIssueBuilder, issue, fieldName, convertedValue)
    }

    private fun convertToMetadataId(fieldName: String, value: Any?, jiraIssue: JiraProprietaryIssue?): Any? {
        val projectKey: String? = jiraIssue?.project?.key
        return when (fieldName) {
            "priorityId" -> JiraMetadata.getPriorityId(value?.toString() ?: "", jiraRestClient)
            "issueTypeId" -> JiraMetadata.getIssueTypeId(value?.toString() ?: "", jiraRestClient)
            "affectedVersions" -> JiraMetadata.getVersionEntity(
                if (value is List<*>) value else listOf(value),
                jiraRestClient,
                projectKey
            )
            else -> value
        }
    }

    private fun convertFromMetadataId(fieldName: String, value: Any): Any {
        return when {
            "priorityId" == fieldName -> JiraMetadata.getPriorityName(value.toString().toLong(), jiraRestClient)
            "versions" == fieldName -> getFirstVersion(value)
            "resolution" == fieldName -> (value as Resolution).name
            value is JSONObject -> value.get("value")
            else -> value
        }
    }

    /**
     *  Jira allows multiple affected versions, while RTC allows only one affected version.
     *  Because of this, only the first entry of the List will be used for synchronization.
     */
    private fun getFirstVersion(value: Any) = ((value as List<*>)[0] as Version).name ?: ""

    private fun getJiraIssue(key: String): JiraProprietaryIssue {
        return jiraRestClient.issueClient.getIssue(key).claim()
    }

    override fun createOrUpdateTargetIssue(
        issue: Issue,
        defaultsForNewIssue: DefaultsForNewIssue?
    ) {
        val targetKeyFieldname = issue.keyFieldMapping!!.getTargetFieldname()
        val targetIssueKey = issue.keyFieldMapping!!.getKeyForTargetIssue().toString()
        val targetIssue =
            (issue.proprietaryTargetInstance ?: if (targetIssueKey.isNotEmpty()) getProprietaryIssue(
                targetKeyFieldname,
                targetIssueKey
            ) else null) as JiraProprietaryIssue?
        when {
            targetIssue != null -> {
                updateTargetIssue(targetIssue, issue)
            }
            defaultsForNewIssue != null -> {
                createTargetIssue(defaultsForNewIssue, issue)
            }
            else -> throw SynchronizationAbortedException("No target issue found for $targetIssueKey, and no defaults for creating issue were provided")
        }
    }

    private fun createTargetIssue(
        defaultsForNewIssue: DefaultsForNewIssue,
        issue: Issue
    ): JiraProprietaryIssue {
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
        defaultsForNewIssue.additionalFields.multiselectFields.forEach {
            val value = ComplexIssueInputFieldValue.with("value", it.value)
            issueBuilder.setFieldValue(it.key, Collections.singletonList(value))
        }
        defaultsForNewIssue.additionalFields.enumerationFields.forEach {
            val value = ComplexIssueInputFieldValue.with("value", it.value)
            issueBuilder.setFieldValue(it.key, value)
        }
        defaultsForNewIssue.additionalFields.simpleTextFields.forEach {
            issueBuilder.setFieldValue(it.key, it.value)
        }
        val basicIssue = jiraRestClient.issueClient.createIssue(issueBuilder.build()).claim()
        logger().info("Created new JIRA issue ${basicIssue.key}")
        issue.workLog.add("Created new JIRA issue ${basicIssue.key}")
        val targetIssue =
            getProprietaryIssue(basicIssue.key) ?: throw IssueClientException("Failed to locate newly created issue")

        updateTargetIssue(targetIssue, issue)
        return targetIssue
    }

    private fun updateTargetIssue(targetIssue: JiraProprietaryIssue, issue: Issue) {
        val issueBuilder = IssueInputBuilder()
        setTargetPropertiesOnSyncIssue(targetIssue, issue)

        issue.fieldMappings.forEach {
            it.setTargetValue(issueBuilder, issue, this)
        }
        logger().info("Updating JIRA issue ${targetIssue.key}")
        val issueInput = issueBuilder.build()
        try {
            jiraRestClient.issueClient.updateIssue(targetIssue.key, issueInput).claim()
        } catch (e: RuntimeException) {
            issue.workLog.add("Failed to update using issue input: $issueInput")
            throw e
        }
    }

    private fun setTargetPropertiesOnSyncIssue(
        targetIssue: JiraProprietaryIssue,
        issue: Issue
    ) {
        issue.proprietaryTargetInstance = targetIssue
        issue.targetKey = getKey(targetIssue)
        issue.targetUrl = getIssueUrl(targetIssue)
    }

    override fun changedIssuesSince(
        lastPollingTimestamp: LocalDateTime,
        batchSize: Int,
        offset: Int
    ): Collection<Issue> {
        val lastPollingTimestampAsString =
            lastPollingTimestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        var jql = "(updated >= '$lastPollingTimestampAsString' OR created >= '$lastPollingTimestampAsString')"
        setup.project?.let { jql += " AND project = '$it'" }
        setup.pollingJqlFilter?.let { jql += " AND $it" }
        try {
            return jiraRestClient.searchClient
                .searchJql("$jql ORDER BY key", batchSize, offset, setOf("*all"))
                .claim()
                .issues
                .map { mapJiraIssue(it) }
                .toList()
        } catch (e: RestClientException) {
            val restExceptionMessage = getRestExceptionMessage(e)
            logger().error(restExceptionMessage)
            throw IllegalStateException(restExceptionMessage)
        }
    }

    override fun getComments(internalIssue: JiraProprietaryIssue): List<Comment> =
        jiraRestClient.getHtmlRenderingRestClient().getHtmlComments(internalIssue.key)

    override fun addComment(internalIssue: JiraProprietaryIssue, comment: Comment) {
        val convertedValue = DefaultWysiwygConverter().convertXHtmlToWikiMarkup(comment.content)
        val jiraComment = com.atlassian.jira.rest.client.api.domain.Comment.valueOf(convertedValue)
        jiraRestClient.issueClient.addComment(internalIssue.commentsUri, jiraComment).claim()
    }

    override fun getAttachments(internalIssue: JiraProprietaryIssue): List<Attachment> =
        internalIssue.attachments?.map { jiraAttachment ->
            val attachmentInputStreamPromise = jiraRestClient.issueClient.getAttachment(jiraAttachment.contentUri)
            attachmentInputStreamPromise.get().use {
                Attachment(
                    jiraAttachment.filename,
                    IOUtils.toByteArray(it)
                )
            }
        } ?: listOf()

    override fun addAttachment(internalIssue: JiraProprietaryIssue, attachment: Attachment) {
        jiraRestClient.issueClient.addAttachment(
            internalIssue.attachmentsUri,
            ByteArrayInputStream(attachment.content),
            attachment.filename
        ).claim()
    }

    override fun getMultiSelectValues(
        internalIssue: JiraProprietaryIssue,
        fieldName: String
    ): List<String> {
        val value = getValue(internalIssue, fieldName) ?: listOf<String>()
        if (value is List<*>) {
            return value.filterIsInstance<String>()
        }
        if (value is JSONObject) {
            return listOf(value["value"].toString())
        }
        val fieldId = internalIssue.getField(fieldName)?.name ?: "no corresponding fieldId"
        throw IllegalArgumentException("The field $fieldName ($fieldId) was expected to return an array, got $value instead. Did you forget to configure the MultiSelectionFieldMapper?")
    }

    override fun getState(internalIssue: JiraProprietaryIssue): String {
        return jiraRestClient.issueClient.getIssue(internalIssue.key).claim().status.name
    }

    override fun getStateHistory(internalIssue: JiraProprietaryIssue): List<StateHistory> {
        val result = jiraRestClient.issueClient.getIssue(internalIssue.key, listOf(IssueRestClient.Expandos.CHANGELOG)).claim()
            .changelog
            ?.flatMap { logEntry ->
                logEntry.items
                    .filter { it.field == "status" }
                    .map { StateHistory(toLocalDateTime(logEntry.created), it.fromString ?: "", it.toString ?: "") }
            } ?: listOf()
        val indexOfLast = maxOf(result.indexOfLast { it.fromState == "Open" }, 0)
        return result.drop(indexOfLast)
    }

    /**
     * JIRA will readily return a list of available transitions on an issue. A [Transition] contains a name,
     * ID, and a `fields` collection holding optional/required fields for the transition.
     */
    override fun setState(
        internalIssue: JiraProprietaryIssue,
        targetState: String
    ) {
        val transition = jiraRestClient.getHtmlRenderingRestClient().getAvailableTransitions(internalIssue.key)
            .filter { it.value == targetState }
            .keys.firstOrNull() ?: throw IllegalArgumentException("No transition found to state $targetState")
        try {
            jiraRestClient.issueClient.transition(internalIssue, TransitionInput(transition.id)).claim()
        } catch (e: Exception) {
            throw IllegalArgumentException("Transition failed for issue ${internalIssue.key} to $targetState", e)
        }
    }

    private fun setResolution(
        internalIssue: JiraProprietaryIssue,
        targetResolution: String
    ) {
        if (internalIssue.resolution?.name ?: "" == targetResolution) return
        if (internalIssue.status.name == "geschlossen") return
        jiraRestClient.getHtmlRenderingRestClient().getAvailableTransitions(internalIssue.key)
            .filter { it.value == "erledigt" }
            .keys.firstOrNull()?.let {
                try {
                    val resolution = FieldInput(IssueFieldId.RESOLUTION_FIELD, ComplexIssueInputFieldValue.with("name", targetResolution))
                    jiraRestClient.issueClient.transition(internalIssue, TransitionInput(it.id, listOf(resolution))).claim()
                } catch (e: Exception) {
                    throw IllegalArgumentException("Transition failed for issue ${internalIssue.key} and resolution $targetResolution", e)
                }
            }
    }

    fun verifySetup(): String {
        return jiraRestClient.metadataClient.serverInfo.claim().serverTitle
    }

    fun listFields() {
        jiraRestClient.metadataClient.fields.claim().forEach { f ->
            println("${f.id} / ${f.name} type ${f.fieldType} ${f.schema?.type}")
        }
    }

    private fun mapJiraIssue(jiraIssue: JiraProprietaryIssue): Issue {
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
        jiraIssue: JiraProprietaryIssue,
        fieldName: String,
        value: Any
    ) {
        val fld = getIssueFieldByNameOrId(jiraIssue, fieldName)
        // you might be tempted to query [metadataClient] directly here. However, JIRA setup allows to map fields
        // to certain projects only, and so finding a field in the metadataClient does NOT mean it is available
        // in the project the issue is assigned to
        when (JiraMetadata.getFieldType(fld.id, jiraRestClient)) {
            // Text custom field
            "string" -> internalIssueBuilder.setFieldValue(fld.id, value.toString())
            "array" -> {
                val writerName = when (JiraMetadata.getFieldCustom(fieldName, jiraRestClient)) {
                    "com.atlassian.jira.plugin.system.customfieldtypes:multiversion" -> "name"
                    else -> "value"
                }
                writeComplexField(value, internalIssueBuilder, fld, jiraIssue, fieldName, writerName)
            }
            "option" -> {
                val complexValue = ComplexIssueInputFieldValue.with("value", value.toString())
                setInternalFieldValue(internalIssueBuilder, fld.id, complexValue)
            }
        }
    }

    private fun writeComplexField(
        value: Any,
        internalIssueBuilder: IssueInputBuilder,
        fld: IssueField,
        jiraIssue: JiraProprietaryIssue,
        fieldName: String,
        fieldWriterName: String
    ): IssueInputBuilder? {
        return if (value is List<*>) {
            val complexValues = createComplexInputFieldValue(value, fieldWriterName)
            internalIssueBuilder.setFieldValue(fld.id, complexValues)
        } else if (value is String) {
            val complexValue = createComplexInputFieldValue(listOf(value), fieldWriterName)
            internalIssueBuilder.setFieldValue(fld.id, complexValue)
        } else {
            val fieldId = jiraIssue.getField(fieldName)?.name ?: "no corresponding fieldId"
            throw IllegalArgumentException("The field $fieldName ($fieldId) was expected to receive an array, but was of type ${value::class.simpleName}")
        }
    }

    private fun createComplexInputFieldValue(
        value: List<*>,
        fieldWriterName: String
    ) = value//
        .map { ComplexIssueInputFieldValue.with(fieldWriterName, it) }

    private fun getCustomFields(
        internalIssue: JiraProprietaryIssue,
        fieldName: String
    ): Any? {
        val field: IssueField = getIssueFieldByNameOrId(internalIssue, fieldName)
        return field.value?.let { getArrayForJsonArrayValue(it) }?.takeIf { !it.isEmpty() } ?: field.value
    }

    private fun getIssueFieldByNameOrId(
        internalIssue: JiraProprietaryIssue,
        fieldName: String
    ): IssueField {
        return internalIssue.getFieldByName(fieldName) ?: internalIssue.getField(fieldName)
        ?: throw IllegalArgumentException("Unknown field $fieldName")
    }

    private fun getArrayForJsonArrayValue(value: Any): MutableList<String> {
        val result: MutableList<String> = mutableListOf()
        if (value is JSONArray) {
            for (i in 0 until value.length()) {
                val jsonObject = value.get(i) as JSONObject
                val fieldValue =
                    if (jsonObject.has("value")) jsonObject.getString("value") else jsonObject.getString("name")
                result.add(fieldValue)
            }
        }
        return result
    }

    private fun setInternalFieldValue(
        internalIssueBuilder: IssueInputBuilder,
        internalFieldId: String,
        internalFieldValue: Any
    ) {
        internalIssueBuilder.setFieldValue(internalFieldId, internalFieldValue)
    }

    private fun toLocalDateTime(jodaDateTime: DateTime): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(jodaDateTime.toInstant().millis), ZoneId.systemDefault())

    override fun getTimeValueInMinutes(
        internalIssue: Any,
        fieldName: String
    ): Number {
        return (getValue(internalIssue as JiraProprietaryIssue, fieldName) ?: 0) as Number
    }

    override fun setTimeValue(internalIssueBuilder: Any, issue: Issue, fieldName: String, timeInMinutes: Number?) {
        val timeInInt = timeInMinutes?.toInt() ?: 0
        val timeNullable = if (timeInInt > 0) timeInInt else null
        setValue(internalIssueBuilder, issue, fieldName, timeNullable)
    }

    override fun logException(
        issue: Issue,
        exception: java.lang.Exception,
        notificationObserver: NotificationObserver,
        syncActions: Map<SyncActionName, SynchronizationAction>
    ) {
        val errorMessage = getRestExceptionMessage(exception)
        logger().debug(errorMessage)
        notificationObserver.notifyException(issue, Exception(errorMessage), syncActions)
    }

    private fun getRestExceptionMessage(exception: java.lang.Exception): String? {
        val errorMessage = if (exception is RestClientException) {
            val statusCode = exception.statusCode.or(0)

            val responseMessage = HttpStatus.valueOf(statusCode).reasonPhrase
            val additionalPhrase = when (statusCode) {
                401, 403 -> "There seems to be a Problem with your Login. Please check your configuration." +
                        " If your login credentials for the tool are correct, then make sure the User is not forced to enter a Captch" +
                        " If a captcha is needed, please shutdown this tool, then manually login and then start this tool again."
                else -> ""
            }
            "Jira: $responseMessage ($statusCode)\n$additionalPhrase"
        } else {
            exception.message
        }
        return errorMessage
    }
}