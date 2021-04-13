package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Comment
import ch.loewenfels.issuetrackingsync.executor.actions.CommentFilter
import java.time.LocalDateTime

class JiraLinkCommentFilter(val filterProperties: Map<String, String> = emptyMap()) : CommentFilter {

    override fun getFilter(): (Comment) -> Boolean = {
        it.content.startsWith("Jira Link:").not()
    }
}