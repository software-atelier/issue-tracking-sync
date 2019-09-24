package ch.loewenfels.issuetrackingsync.syncclient.rtc

import ch.loewenfels.issuetrackingsync.*
import ch.loewenfels.issuetrackingsync.syncclient.IssueClientException
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import com.fasterxml.jackson.databind.JsonNode
import com.ibm.team.foundation.common.text.XMLString
import com.ibm.team.process.client.IProcessClientService
import com.ibm.team.process.common.IProjectArea
import com.ibm.team.repository.client.ITeamRepository
import com.ibm.team.repository.client.TeamPlatform
import com.ibm.team.repository.common.IContent
import com.ibm.team.workitem.client.*
import com.ibm.team.workitem.common.IAuditableCommon
import com.ibm.team.workitem.common.IWorkItemCommon
import com.ibm.team.workitem.common.expression.*
import com.ibm.team.workitem.common.model.*
import com.ibm.team.workitem.common.query.IQueryResult
import com.ibm.team.workitem.common.query.IResolvedResult
import org.eclipse.core.runtime.NullProgressMonitor
import org.springframework.beans.BeanWrapperImpl
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.URLEncoder
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

open class RtcClient(private val setup: IssueTrackingApplication) : IssueTrackingClient<IWorkItem>, Logging {
    private val progressMonitor = NullProgressMonitor()
    private val teamRepository: ITeamRepository
    private val workItemClient: IWorkItemClient
    private val projectArea: IProjectArea

    init {
        teamRepository = TeamPlatform.getTeamRepositoryService().getTeamRepository(setup.endpoint)
        teamRepository.registerLoginHandler(LoginHandler())
        teamRepository.login(NullProgressMonitor())
        workItemClient = teamRepository.getClientLibrary(IWorkItemClient::class.java) as IWorkItemClient
        val processClient = teamRepository.getClientLibrary(IProcessClientService::class.java) as IProcessClientService
        val uri = URI.create(
            setup.project?.replace(" ", "%20") ?: throw IllegalStateException(
                "Need project for RTC client"
            )
        )
        projectArea = processClient.findProcessArea(uri, null, null) as IProjectArea?
            ?: throw IllegalStateException("Project area ${setup.project} is invalid")
    }

    override fun getIssue(key: String): Issue? {
        return getRtcIssue(key)?.let {
            toSyncIssue(it)
        }
    }

    override fun getIssueFromWebhookBody(body: JsonNode): Issue =
        throw UnsupportedOperationException("RTC does not support webhooks")

    override fun getProprietaryIssue(issueKey: String): IWorkItem? {
        return getRtcIssue(issueKey)
    }

    override fun getProprietaryIssue(fieldName: String, fieldValue: String): IWorkItem? {
        val queryClient = workItemClient.queryClient
        val attrExpression =
            AttributeExpression(
                getQueryableAttribute(fieldName),
                AttributeOperation.EQUALS,
                fieldValue
            )
        val resolvedResultOfWorkItems =
            queryClient.getResolvedExpressionResults(projectArea, attrExpression, IWorkItem.FULL_PROFILE)
        val workItems = toWorkItems(resolvedResultOfWorkItems)
        return when (workItems.size) {
            0 -> null
            // reload to get full issue incl. collections such as comments
            1 -> getProprietaryIssue(workItems[0].id.toString())
            else -> throw IssueClientException("Query too broad, multiple issues found for $fieldValue")
        }
    }

    private fun getRtcIssue(key: String): IWorkItem? {
        return workItemClient.findWorkItemById(Integer.parseInt(key), IWorkItem.SMALL_PROFILE, progressMonitor)
    }

    override fun getLastUpdated(internalIssue: IWorkItem): LocalDateTime =
        LocalDateTime.ofInstant(internalIssue.modified().toInstant(), ZoneId.systemDefault())

    override fun getKey(internalIssue: IWorkItem): String =
        internalIssue.id.toString()

    override fun getIssueUrl(internalIssue: IWorkItem): String {
        val endpoint =
            if (setup.endpoint.endsWith("/")) setup.endpoint.substring(0, setup.endpoint.length - 1) else setup.endpoint
        val encodedProjectName = URLEncoder.encode(setup.project, "UTF-8").replace("+", "%20")
        return "$endpoint/web/projects/$encodedProjectName#action=com.ibm.team.workitem.viewWorkItem&id=${internalIssue.id}"
    }

    override fun getValue(internalIssue: IWorkItem, fieldName: String): Any? {
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
            val workItem = internalIssueBuilder as IWorkItem
            val attribute = getAttribute(fieldName)
            workItem.setValue(attribute, it)
        }
    }

    private fun convertToMetadataId(fieldName: String, value: Any?): Any? {
        return when (fieldName) {
            "priority", "internalPriority" -> RtcMetadata.getPriorityId(
                value?.toString() ?: "",
                getAttribute(IWorkItem.PRIORITY_PROPERTY),
                workItemClient
            )
            "severity", "internalSeverity" -> RtcMetadata.getSeverityId(
                value?.toString() ?: "",
                getAttribute(IWorkItem.SEVERITY_PROPERTY),
                workItemClient
            )
            else -> value
        }
    }

    private fun convertFromMetadataId(fieldName: String, value: Any): Any {
        return when (fieldName) {
            "priority", "internalPriority" -> RtcMetadata.getPriorityName(
                value.toString(),
                getAttribute(IWorkItem.PRIORITY_PROPERTY),
                workItemClient
            )
            "severity", "internalSeverity" -> RtcMetadata.getSeverity(
                value.toString(),
                getAttribute(IWorkItem.SEVERITY_PROPERTY),
                workItemClient
            )
            else -> value
        }
    }

    override fun createOrUpdateTargetIssue(
        issue: Issue,
        defaultsForNewIssue: DefaultsForNewIssue?
    ) {
        val targetKeyFieldname = issue.keyFieldMapping!!.getTargetFieldname()
        val targetIssueKey = issue.keyFieldMapping!!.getKeyForTargetIssue().toString()
        var targetIssue =
            (issue.proprietaryTargetInstance ?: if (targetIssueKey.isNotEmpty()) getProprietaryIssue(
                targetKeyFieldname,
                targetIssueKey
            ) else null) as IWorkItem?
        when {
            targetIssue != null -> updateTargetIssue(targetIssue, issue)
            defaultsForNewIssue != null -> targetIssue = createTargetIssue(defaultsForNewIssue, issue)
            else -> throw SynchronizationAbortedException("No target issue found for $targetIssueKey, and no defaults for creating issue were provided")
        }
        issue.proprietaryTargetInstance = targetIssue
        issue.targetUrl = getIssueUrl(targetIssue)
    }

    private fun createTargetIssue(defaultsForNewIssue: DefaultsForNewIssue, issue: Issue): IWorkItem {
        val workItemType: IWorkItemType =
            workItemClient.findWorkItemType(projectArea, defaultsForNewIssue.issueType, progressMonitor)
        val path = defaultsForNewIssue.category.split("/")
        val category: ICategoryHandle = workItemClient.findCategoryByNamePath(projectArea, path, progressMonitor)
        val operation = WorkItemInitialization("creating new issue", category)
        val handle: IWorkItemHandle = operation.run(workItemType, progressMonitor)
        operation.workItem?.let { mapNewIssueValues(it, issue) }
        val auditableClient: IAuditableClient =
            teamRepository.getClientLibrary(IAuditableClient::class.java) as IAuditableClient
        val workItem: IWorkItem = auditableClient.resolveAuditable(handle, IWorkItem.FULL_PROFILE, progressMonitor)
        logger().info("Created new RTC issue ${workItem.id}")
        return workItem
    }

    private fun updateTargetIssue(targetIssue: IWorkItem, issue: Issue) {
        doWithWorkingCopy(targetIssue) {
            val changeableWorkingItem = it.workItem
            mapNewIssueValues(changeableWorkingItem, issue)
            logger().info("Updating RTC issue ${targetIssue.id}")
        }
    }

    private fun mapNewIssueValues(targetIssue: IWorkItem, issue: Issue) {
        issue.fieldMappings.forEach {
            it.setTargetValue(targetIssue, issue, this)
        }
    }

    override fun changedIssuesSince(lastPollingTimestamp: LocalDateTime): Collection<Issue> {
        val queryClient = workItemClient.queryClient
        val searchTerms = buildSearchTermForChangedIssues(lastPollingTimestamp)
        val resolvedResultOfWorkItems =
            queryClient.getResolvedExpressionResults(projectArea, searchTerms, IWorkItem.FULL_PROFILE)
        return toWorkItems(resolvedResultOfWorkItems).map { toSyncIssue(it) }
    }

    private fun buildSearchTermForChangedIssues(lastPollingTimestamp: LocalDateTime): Term {
        val modifiedRecently =
            AttributeExpression(
                getQueryableAttribute(IWorkItem.MODIFIED_PROPERTY),
                AttributeOperation.GREATER_OR_EQUALS,
                Timestamp.valueOf(lastPollingTimestamp)
            )
        val createdRecently =
            AttributeExpression(
                getQueryableAttribute(IWorkItem.CREATION_DATE_PROPERTY),
                AttributeOperation.GREATER_OR_EQUALS,
                Timestamp.valueOf(lastPollingTimestamp)
            )
        val projectAreaExpression = AttributeExpression(
            getQueryableAttribute(IWorkItem.PROJECT_AREA_PROPERTY),
            AttributeOperation.EQUALS,
            projectArea
        )
        val relevantIssuesTerm = Term(Term.Operator.OR)
        relevantIssuesTerm.add(modifiedRecently)
        relevantIssuesTerm.add(createdRecently)
        //
        val searchTerm = Term(Term.Operator.AND)
        searchTerm.add(relevantIssuesTerm)
        searchTerm.add(projectAreaExpression)
        return searchTerm
    }

    private fun getQueryableAttribute(attributeName: String): IQueryableAttribute {
        val auditableCommon: IAuditableCommon =
            teamRepository.getClientLibrary(IAuditableCommon::class.java) as IAuditableCommon
        return QueryableAttributes.getFactory(IWorkItem.ITEM_TYPE).findAttribute(
            projectArea,
            attributeName,
            auditableCommon,
            progressMonitor
        ) ?: throw IllegalArgumentException("Attribute $attributeName is either unknown or not query-able")
    }

    private fun getAttribute(attributeName: String): IAttribute =
        workItemClient.findAttribute(
            projectArea,
            attributeName,
            progressMonitor
        ) ?: throw IllegalArgumentException("Unknown attribute $attributeName")

    private fun toWorkItems(resolvedResults: IQueryResult<IResolvedResult<IWorkItem>>): List<IWorkItem> {
        val result = LinkedList<IWorkItem>()
        while (resolvedResults.hasNext(progressMonitor)) {
            result.add(resolvedResults.next(progressMonitor).item)
        }
        return result
    }

    private fun toSyncIssue(workItem: IWorkItem): Issue {
        return Issue(
            workItem.id.toString(),
            setup.name,
            getLastUpdated(workItem)
        )
    }

    override fun getComments(internalIssue: IWorkItem): List<Comment> =
        internalIssue.comments.contents.map { rtcComment ->
            Comment(
                rtcComment.creator.toString(),
                rtcComment.creationDate.toLocalDateTime(),
                rtcComment.htmlContent.plainText
            )
        }

    override fun addComment(internalIssue: IWorkItem, comment: Comment) {
        doWithWorkingCopy(internalIssue) {
            val changeableWorkingItem = it.workItem
            val comments = changeableWorkingItem.comments
            val newComment = comments.createComment(
                teamRepository.loggedInContributor(),
                XMLString.createFromXMLText(comment.content)
            )
            comments.append(newComment)
        }
    }

    override fun getAttachments(internalIssue: IWorkItem): List<Attachment> {
        val auditableClient = teamRepository.getClientLibrary(IAuditableClient::class.java) as IAuditableClient
        val common = teamRepository.getClientLibrary(IWorkItemCommon::class.java) as IWorkItemCommon
        return common.resolveWorkItemReferences(internalIssue, progressMonitor)
            .getReferences(WorkItemEndPoints.ATTACHMENT)
            .map {
                val attachHandle = it.resolve() as IAttachmentHandle
                val attachment = auditableClient.resolveAuditable(
                    attachHandle,
                    IAttachment.DEFAULT_PROFILE, null
                ) as IAttachment
                val baos = ByteArrayOutputStream()
                teamRepository.contentManager().retrieveContent(attachment.content, baos, null);
                Attachment(attachment.name, baos.toByteArray())
            }
    }

    override fun addAttachment(internalIssue: IWorkItem, attachment: Attachment) {
        doWithWorkingCopy(internalIssue) {
            val contentType = IContent.CONTENT_TYPE_UNKNOWN // or IContent.CONTENT_TYPE_TEXT?
            val encoding = IContent.ENCODING_UTF_8
            var newAttachment = workItemClient.createAttachment(
                projectArea, attachment.filename, "", contentType,
                encoding, ByteArrayInputStream(attachment.content), progressMonitor
            )
            newAttachment = newAttachment.workingCopy as IAttachment
            newAttachment = workItemClient.saveAttachment(newAttachment, progressMonitor)
            val reference = WorkItemLinkTypes.createAttachmentReference(newAttachment)
            it.references.add(WorkItemEndPoints.ATTACHMENT, reference)
        }
    }

    private fun doWithWorkingCopy(originalWorkItem: IWorkItem, consumer: (WorkItemWorkingCopy) -> Unit) {
        val copyManager = workItemClient.workItemWorkingCopyManager
        copyManager.connect(originalWorkItem, IWorkItem.FULL_PROFILE, progressMonitor)
        try {
            val workingCopy = copyManager.getWorkingCopy(originalWorkItem)
            consumer.invoke(workingCopy)
            val detailedStatus = workingCopy.save(null)
            if (!detailedStatus.isOK) {
                throw  RuntimeException("Error saving work item", detailedStatus.exception)
            }
        } finally {
            copyManager.disconnect(originalWorkItem)
        }
    }

    fun listMetadata(): List<IAttribute> =
        workItemClient.findAttributes(projectArea, progressMonitor).toList()

    inner class LoginHandler : ITeamRepository.ILoginHandler, ITeamRepository.ILoginHandler.ILoginInfo {
        override fun getUserId(): String {
            return setup.username
        }

        override fun getPassword(): String {
            return setup.password
        }

        override fun challenge(repository: ITeamRepository?): ITeamRepository.ILoginHandler.ILoginInfo {
            return this
        }
    }
}
