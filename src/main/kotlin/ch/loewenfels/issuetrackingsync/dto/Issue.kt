package ch.loewenfels.issuetrackingsync.dto

import java.time.LocalDateTime

data class Issue(val key: String, val clientSourceName: String, val lastUpdated: LocalDateTime)