package ch.loewenfels.issuetrackingsync

import java.time.LocalDateTime

data class Comment(val author: String, val timestamp: LocalDateTime, val content: String)