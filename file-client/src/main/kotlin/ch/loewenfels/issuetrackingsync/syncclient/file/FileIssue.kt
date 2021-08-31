package ch.loewenfels.issuetrackingsync.syncclient.file

import ch.loewenfels.issuetrackingsync.Attachment
import ch.loewenfels.issuetrackingsync.Comment
import java.io.Serializable
import java.time.LocalDateTime

data class FileIssue(
    var summary: String,
    var status: String,
    var description: String,
    var lastUpdated: LocalDateTime,
    val attributes: MutableMap<String, String> = HashMap(),
) : Serializable {
    var key: String = ""
    var comments: List<Comment> = listOf()
    var attachments: List<Attachment> = listOf()
    var doNotSynchronize: String? = null
}
