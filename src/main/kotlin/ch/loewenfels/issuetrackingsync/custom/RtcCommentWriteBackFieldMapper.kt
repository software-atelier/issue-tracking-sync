package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Comment
import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.actions.CommentsSynchronizationAction
import ch.loewenfels.issuetrackingsync.executor.fields.FieldMapper
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import java.time.LocalDateTime

class RtcCommentWriteBackFieldMapper : FieldMapper {
    override fun <T> getValue(
        proprietaryIssue: T,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>
    ) = "$fieldname ${issueTrackingClient.getIssueUrl(proprietaryIssue)}"

    private val commentId = "00000000"

    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        val comments = issueTrackingClient.getComments(issue.proprietaryTargetInstance as T)
        val comment = Comment(
            "",
            LocalDateTime.now(),
            value as String,
            commentId
        )
        if (CommentsSynchronizationAction.isSourcePresentInTarget(comment, comments).not()) {
            issueTrackingClient.addComment(issue.proprietaryTargetInstance as T, comment)
        }
    }
}