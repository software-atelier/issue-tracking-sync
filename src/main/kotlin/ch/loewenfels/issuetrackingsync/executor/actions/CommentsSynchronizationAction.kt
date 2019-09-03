package ch.loewenfels.issuetrackingsync.executor.actions

import ch.loewenfels.issuetrackingsync.*
import ch.loewenfels.issuetrackingsync.executor.fields.FieldMapping
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue

class CommentsSynchronizationAction : AbstractSynchronizationAction(),
    SynchronizationAction, Logging {
    override fun execute(
        sourceClient: IssueTrackingClient<Any>,
        targetClient: IssueTrackingClient<Any>,
        issue: Issue,
        fieldMappings: List<FieldMapping>,
        defaultsForNewIssue: DefaultsForNewIssue?
    ) {
        val internalSourceIssue = issue.proprietarySourceInstance
        val internalTargetIssue = issue.proprietaryTargetInstance
        if ((internalSourceIssue != null) && (internalTargetIssue != null)) {
            val sourceComments = sourceClient.getComments(internalSourceIssue)
            val targetComments = targetClient.getComments(internalTargetIssue)
            val commentsToSync = getSourceCommentsNotPresentInTarget(sourceComments, targetComments)
            commentsToSync.forEach {
                targetClient.addComment(internalTargetIssue, it)
                issue.workLog.add("Added comment from ${it.author}")
            }
        } else {
            logger().warn("This action relies on a previous action loading source and target issues. Consider configuring a SimpleSynchronizationAction without any fieldMappings prior to this action")
        }
    }

    private fun getSourceCommentsNotPresentInTarget(
        sourceComments: List<Comment>,
        targetComments: List<Comment>
    ): List<Comment> =
        sourceComments.filter { src -> !isSourcePresentInTarget(src, targetComments) }.toList()

    private fun isSourcePresentInTarget(
        sourceComment: Comment,
        targetComments: List<Comment>
    ): Boolean =
        targetComments.any { targetComment ->
            sourceComment.content.contains(targetComment.content) || targetComment.content.contains(
                sourceComment.content
            )
        }
}