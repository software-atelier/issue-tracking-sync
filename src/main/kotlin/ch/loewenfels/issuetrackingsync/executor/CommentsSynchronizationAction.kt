package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.Comment
import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue

class CommentsSynchronizationAction : AbstractSynchronizationAction(), SynchronizationAction {
    override fun execute(
        sourceClient: IssueTrackingClient<Any>,
        targetClient: IssueTrackingClient<Any>,
        issue: Issue,
        fieldMappings: List<FieldMapping>,
        defaultsForNewIssue: DefaultsForNewIssue?
    ) {
        val sourceComments = issue.proprietarySourceInstance?.let { sourceClient.getComments(it) } ?: mutableListOf()
        val targetComments = issue.proprietaryTargetInstance?.let { targetClient.getComments(it) } ?: mutableListOf()
        val commentsToSync = getSourceCommentsNotPresentInTarget(sourceComments, targetComments)
        commentsToSync.forEach {
            targetClient.addComment(issue, it)
            issue.workLog.add("Added comment from ${it.author}")
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