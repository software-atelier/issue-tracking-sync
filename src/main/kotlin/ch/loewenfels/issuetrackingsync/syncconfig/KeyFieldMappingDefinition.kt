package ch.loewenfels.issuetrackingsync.syncconfig

import ch.loewenfels.issuetrackingsync.executor.DirectFieldMapper

data class KeyFieldMappingDefinition(
    var sourceName: String = "",
    var targetName: String = "",
    var writeBackToSourceName: String = "",
    var mapperClassname: String = DirectFieldMapper::class.qualifiedName ?: ""
)