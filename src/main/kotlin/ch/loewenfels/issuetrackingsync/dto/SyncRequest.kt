package ch.loewenfels.issuetrackingsync.dto

import java.io.Serializable
import java.time.LocalDateTime

data class SyncRequest(val key: String, val clientSourceName: String, val lastUpdated: LocalDateTime) : Serializable
