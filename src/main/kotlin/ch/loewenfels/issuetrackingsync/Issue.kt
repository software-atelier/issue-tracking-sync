package ch.loewenfels.issuetrackingsync

import java.time.LocalDateTime

data class Issue(val key: String, val clientSourceName: String, val lastUpdated: LocalDateTime)