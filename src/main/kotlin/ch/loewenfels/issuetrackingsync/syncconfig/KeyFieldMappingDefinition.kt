package ch.loewenfels.issuetrackingsync.syncconfig

import ch.loewenfels.issuetrackingsync.executor.DirectFieldMapper

class KeyFieldMappingDefinition(
    sourceName: String = "",
    targetName: String = "",
    mapperClassname: String = DirectFieldMapper::class.qualifiedName ?: "",
    associations: MutableMap<String, String> = mutableMapOf(),
    var writeBackToSourceName: String = ""
) : FieldMappingDefinition(sourceName, targetName, mapperClassname, associations)