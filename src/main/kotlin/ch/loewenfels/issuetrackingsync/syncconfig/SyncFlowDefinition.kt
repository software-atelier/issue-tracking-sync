package ch.loewenfels.issuetrackingsync.syncconfig

data class SyncFlowDefinition(
    var name: String = "",
    var source: TrackingApplicationName = "",
    var target: TrackingApplicationName = "",
    var filterClassname: String? = null,
    var actionClassname: String = "",
    var defaultsForNewIssue: DefaultsForNewIssue? = null,
    var keyFieldMappingDefinition: FieldMappingDefinition = FieldMappingDefinition("key", "key"),
    var fieldMappingDefinitions: MutableList<FieldMappingDefinition> = mutableListOf()
)
