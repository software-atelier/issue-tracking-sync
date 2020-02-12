package ch.loewenfels.issuetrackingsync.executor.actions

import ch.loewenfels.issuetrackingsync.*
import ch.loewenfels.issuetrackingsync.executor.fields.FieldMapping
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.AdditionalProperties
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import java.time.format.DateTimeFormatter

class CommentsSynchronizationAction : AbstractSynchronizationAction(),
    SynchronizationAction, Logging {
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm")
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun execute(
        sourceClient: IssueTrackingClient<Any>,
        targetClient: IssueTrackingClient<Any>,
        issue: Issue,
        fieldMappings: List<FieldMapping>,
        defaultsForNewIssue: DefaultsForNewIssue?
    ) = execute(sourceClient, targetClient, issue, fieldMappings, defaultsForNewIssue, AdditionalProperties())

    override fun execute(
        sourceClient: IssueTrackingClient<Any>,
        targetClient: IssueTrackingClient<Any>,
        issue: Issue,
        fieldMappings: List<FieldMapping>,
        defaultsForNewIssue: DefaultsForNewIssue?,
        additionalProperties: AdditionalProperties?
    ) {
        val internalSourceIssue = issue.proprietarySourceInstance
        val internalTargetIssue = issue.proprietaryTargetInstance
        if ((internalSourceIssue != null) && (internalTargetIssue != null)) {
            val sourceComments = sourceClient.getComments(internalSourceIssue)
            val targetComments = targetClient.getComments(internalTargetIssue)
            val commentsToSync = getSourceCommentsNotPresentInTarget(sourceComments, targetComments)
            commentsToSync //
                .map { mapContentOfComment(it, additionalProperties) }//
                .forEach {
                    targetClient.addComment(internalTargetIssue, it)
                    issue.workLog.add("Added comment from ${it.author}")
                }
        } else {
            logger().warn("This action relies on a previous action loading source and target issues." +
                    " Consider configuring a SimpleSynchronizationAction without any fieldMappings prior to this action.")
        }
    }

    private fun mapContentOfComment(comment: Comment, additionalProperties: AdditionalProperties?): Comment {
        val preComment: String = replacePlaceholders(additionalProperties?.preComment ?: "", comment)
        val postComment: String = replacePlaceholders(additionalProperties?.postComment ?: "", comment)
        val content = preComment + comment.content + postComment
        return Comment(comment.author, comment.timestamp, content)
    }

    private fun replacePlaceholders(
        containingPlaceholders: String,
        comment: Comment
    ): String {
        return containingPlaceholders
            .replace("\${author}", comment.author)//
            .replace("\${time}", comment.timestamp.format(timeFormatter)) //
            .replace("\${date}", comment.timestamp.format(dateFormatter)) //
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