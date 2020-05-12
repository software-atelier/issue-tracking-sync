package ch.loewenfels.issuetrackingsync

import ch.loewenfels.issuetrackingsync.executor.fields.FieldMapping
import ch.loewenfels.issuetrackingsync.executor.fields.KeyFieldMapping
import java.time.LocalDateTime

data class Issue(val key: String, val clientSourceName: String, val lastUpdated: LocalDateTime) {
    var proprietarySourceInstance: Any? = null
    var sourceUrl: String? = null
    var keyFieldMapping: KeyFieldMapping? = null
    var targetUrl: String? = null

    /**
     * Initially empty, set by an issue client once a write operation takes place
     */
    var lastUpdatedBy: String = ""
    var proprietaryTargetInstance: Any? = null
    var targetKey: String? = null
    val fieldMappings: MutableList<FieldMapping> = mutableListOf()
    val workLog: MutableList<String> = mutableListOf()
}