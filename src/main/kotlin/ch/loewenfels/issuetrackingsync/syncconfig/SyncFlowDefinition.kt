package ch.loewenfels.issuetrackingsync.syncconfig

data class SyncFlowDefinition(
    var name: String = "",
    var source: TrackingApplicationName = "",
    var target: TrackingApplicationName = "",
    var filterClassname: String? = null,
    var defaultsForNewIssue: DefaultsForNewIssue? = null,
    var keyFieldMappingDefinition: KeyFieldMappingDefinition = KeyFieldMappingDefinition(),
    var actions: MutableList<String> = mutableListOf()
)
