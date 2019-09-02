package ch.loewenfels.issuetrackingsync

import ch.loewenfels.issuetrackingsync.executor.FieldMapping
import ch.loewenfels.issuetrackingsync.executor.KeyFieldMapping
import java.time.LocalDateTime

data class Issue(val key: String, val clientSourceName: String, val lastUpdated: LocalDateTime) {
    var proprietarySourceInstance: Any? = null
    var keyFieldMapping: KeyFieldMapping? = null
    var writeBackFieldMapping: KeyFieldMapping? = null
    val fieldMappings: MutableList<FieldMapping> = mutableListOf()
}