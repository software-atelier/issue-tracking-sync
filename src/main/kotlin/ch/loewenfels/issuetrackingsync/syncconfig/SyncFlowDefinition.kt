package ch.loewenfels.issuetrackingsync.syncconfig

data class SyncFlowDefinition(
    var name: String = "",
    var source: TrackingApplicationName = "",
    var target: TrackingApplicationName = "",
    var filterClassname: String? = null,
    var defaultsForNewIssue: DefaultsForNewIssue? = null,
    var keyFieldMappingDefinition: FieldMappingDefinition = FieldMappingDefinition(),
    var writeBackFieldMappingDefinition: FieldMappingDefinition? = null,
    var actions: MutableList<String> = mutableListOf()
)
