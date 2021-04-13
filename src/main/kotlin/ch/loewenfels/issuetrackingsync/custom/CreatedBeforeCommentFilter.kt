package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Comment
import ch.loewenfels.issuetrackingsync.executor.actions.CommentFilter
import java.time.LocalDateTime

const val CREATED_BEFORE = "createdBefore"

class CreatedBeforeCommentFilter(val filterProperties: Map<String, String> = emptyMap()) : CommentFilter {

    override fun getFilter(): (Comment) -> Boolean = {
        !filterProperties.containsKey(CREATED_BEFORE)
                || !it.timestamp.isBefore(LocalDateTime.parse(filterProperties[CREATED_BEFORE]))
    }
}