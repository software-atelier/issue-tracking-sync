package ch.loewenfels.issuetrackingsync.syncconfig

import com.fasterxml.jackson.annotation.JsonFormat

data class SyncFlowDefinition(
    var name: String = "",
    var source: TrackingApplicationName = "",
    var target: TrackingApplicationName = "",
    var filterClassname: String? = null,
    var defaultsForNewIssue: DefaultsForNewIssue? = null,
    var keyFieldMappingDefinition: FieldMappingDefinition = FieldMappingDefinition(),
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    var writeBackFieldMappingDefinition: List<FieldMappingDefinition> = emptyList(),
    var actions: MutableList<String> = mutableListOf()
)
