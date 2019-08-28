package ch.loewenfels.issuetrackingsync.executor

class KeyFieldMapping(
    sourceName: String,
    targetName: String,
    mapper: FieldMapper
) : FieldMapping(sourceName, targetName, mapper) {
    fun getKeyForTargetIssue(): Any? = sourceValue
}