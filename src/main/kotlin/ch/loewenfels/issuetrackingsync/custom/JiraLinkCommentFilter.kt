package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Comment
import ch.loewenfels.issuetrackingsync.executor.actions.CommentFilter

class JiraLinkCommentFilter : CommentFilter {
    override fun getFilter(): (Comment) -> Boolean = {
        it.content.startsWith("Jira Link:").not()
    }
}