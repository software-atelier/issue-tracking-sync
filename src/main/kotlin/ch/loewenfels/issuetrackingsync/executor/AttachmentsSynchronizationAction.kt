package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.Attachment
import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue

class AttachmentsSynchronizationAction : AbstractSynchronizationAction(), SynchronizationAction {
    override fun execute(
        sourceClient: IssueTrackingClient<Any>,
        targetClient: IssueTrackingClient<Any>,
        issue: Issue,
        fieldMappings: List<FieldMapping>,
        defaultsForNewIssue: DefaultsForNewIssue?
    ) {
        val sourceAttachments =
            issue.proprietarySourceInstance?.let { sourceClient.getAttachments(it) } ?: mutableListOf()
        val targetAttachments =
            issue.proprietaryTargetInstance?.let { targetClient.getAttachments(it) } ?: mutableListOf()
        val attachmentsToSync = getSourceAttachmentsNotPresentInTarget(sourceAttachments, targetAttachments)
        attachmentsToSync.forEach {
            targetClient.addAttachment(issue, it)
            issue.workLog.add("Added attachment ${it.filename}")
        }
    }

    private fun getSourceAttachmentsNotPresentInTarget(
        sourceAttachments: List<Attachment>,
        targetAttachments: List<Attachment>
    ): List<Attachment> =
        sourceAttachments.filter { src -> !targetAttachments.contains(src) }.toList()
}