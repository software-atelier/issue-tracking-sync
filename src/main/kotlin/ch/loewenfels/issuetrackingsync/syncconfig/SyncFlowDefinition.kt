package ch.loewenfels.issuetrackingsync.syncconfig

data class SyncFlowDefinition(
    var name: String = "",
    var source: TrackingApplicationName = "",
    var target: TrackingApplicationName = "",
    var filterClassname: String? = null,
    var actionClassname: String = "",
    var fieldMappings: MutableList<FieldMapping> = mutableListOf()
)
