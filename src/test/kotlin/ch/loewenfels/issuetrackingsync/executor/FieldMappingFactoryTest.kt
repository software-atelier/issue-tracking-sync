package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class FieldMappingFactoryTest {
    @Test
    fun getMapper_validClassName_instanceLoaded() {
        // arrange
        val directMapping = buildFieldMappingDefinition(DirectFieldMapper::class.qualifiedName ?: "")
        // act
        val mapperInstance = FieldMappingFactory.getMapping(directMapping)
        // assert
        assertNotNull(mapperInstance);
    }

    @Test
    fun getMapper_invalidClassName_exception() {
        // arrange
        val unknownMapping = buildFieldMappingDefinition("ch.loewenfels.MapperDoesntExist")
        // act
        assertThrows(IllegalArgumentException::class.java) { FieldMappingFactory.getMapping(unknownMapping) }
    }

    private fun buildFieldMappingDefinition(className: String) = FieldMappingDefinition("", "", className)
}