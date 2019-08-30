package ch.loewenfels.issuetrackingsync.syncconfig

data class SyncActionDefinition(
    var name: String = "",
    var classname: String = "",
    var fieldMappingDefinitions: MutableList<FieldMappingDefinition> = mutableListOf()
)