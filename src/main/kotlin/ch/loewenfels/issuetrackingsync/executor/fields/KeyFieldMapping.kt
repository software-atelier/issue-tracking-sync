package ch.loewenfels.issuetrackingsync.executor.fields

class KeyFieldMapping(
    sourceName: String,
    targetName: String,
    mapper: FieldMapper
) : FieldMapping(sourceName, targetName, mapper) {
    fun getKeyForSourceIssue(): Any? = sourceValue
    fun getTargetFieldname(): String = targetName
    fun invertMapping(): KeyFieldMapping = KeyFieldMapping(targetName, sourceName, mapper)
}