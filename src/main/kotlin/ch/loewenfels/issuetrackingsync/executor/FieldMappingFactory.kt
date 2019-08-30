package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import ch.loewenfels.issuetrackingsync.syncconfig.KeyFieldMappingDefinition

object FieldMappingFactory {
    private val mapperInstances = mutableMapOf<String, FieldMapper>()

    fun getMapping(fieldMappingDefinition: FieldMappingDefinition): FieldMapping = FieldMapping(
        fieldMappingDefinition.sourceName,
        fieldMappingDefinition.targetName,
        getMapper(fieldMappingDefinition.mapperClassname)
    )

    fun getKeyMapping(fieldMappingDefinition: KeyFieldMappingDefinition): KeyFieldMapping = KeyFieldMapping(
        fieldMappingDefinition.sourceName,
        fieldMappingDefinition.targetName,
        fieldMappingDefinition.writeBackToSourceName,
        getMapper(fieldMappingDefinition.mapperClassname)
    )

    private fun getMapper(mapperClassname: String): FieldMapper {
        return try {
            mapperInstances.computeIfAbsent(mapperClassname) {
                val mapperClass = Class.forName(it) as Class<FieldMapper>
                mapperClass.newInstance()
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to load field mapper class $mapperClassname", e)
        }
    }
}