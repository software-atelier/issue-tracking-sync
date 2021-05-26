package ch.loewenfels.issuetrackingsync.syncconfig

import ch.loewenfels.issuetrackingsync.executor.fields.DirectFieldMapper

open class FieldMappingDefinition(
    var sourceName: String = "",
    var targetName: String = "",
    var mapperClassname: String = DirectFieldMapper::class.qualifiedName ?: "",
    var fieldSkipEvaluators: MutableList<FieldSkippingEvaluatorDefinition> = mutableListOf(),
    var callback: FieldMappingDefinition? = null,
    associations: MutableMap<String, String> = mutableMapOf()
) : AssociationsFieldDefinition(associations)
