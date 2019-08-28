package ch.loewenfels.issuetrackingsync.syncconfig

import ch.loewenfels.issuetrackingsync.executor.DirectFieldMapper

data class FieldMappingDefinition(
    var sourceName: String = "",
    var targetName: String = "",
    var mapperClassname: String = DirectFieldMapper::class.qualifiedName ?: ""
)