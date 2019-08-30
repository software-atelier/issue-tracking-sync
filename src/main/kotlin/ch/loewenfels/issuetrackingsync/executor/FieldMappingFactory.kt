package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import ch.loewenfels.issuetrackingsync.syncconfig.KeyFieldMappingDefinition

object FieldMappingFactory {
    private val mapperInstances = mutableMapOf<String, FieldMapper>()

    fun getMapping(fieldMappingDefinition: FieldMappingDefinition): FieldMapping = FieldMapping(
        fieldMappingDefinition.sourceName,
        fieldMappingDefinition.targetName,
        getMapper(fieldMappingDefinition)
    )

    fun getKeyMapping(fieldMappingDefinition: KeyFieldMappingDefinition): KeyFieldMapping = KeyFieldMapping(
        fieldMappingDefinition.sourceName,
        fieldMappingDefinition.targetName,
        fieldMappingDefinition.writeBackToSourceName,
        getMapper(fieldMappingDefinition)
    )

    private fun getMapper(fieldMappingDefinition: FieldMappingDefinition): FieldMapper {
        return mapperInstances.computeIfAbsent(fieldMappingDefinition.mapperClassname) {
            buildMapper(fieldMappingDefinition)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildMapper(fieldMappingDefinition: FieldMappingDefinition): FieldMapper {
        val mapperClass = try {
            Class.forName(fieldMappingDefinition.mapperClassname) as Class<FieldMapper>
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Failed to load field mapper class ${fieldMappingDefinition.mapperClassname}",
                e
            )
        }
        return try {
            mapperClass.getConstructor(FieldMappingDefinition::class.java).newInstance(fieldMappingDefinition)
        } catch (e: Exception) {
            null;
        } ?: try {
            mapperClass.newInstance()
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to instantiate mapper class $mapperClass", e)
        }
    }
}