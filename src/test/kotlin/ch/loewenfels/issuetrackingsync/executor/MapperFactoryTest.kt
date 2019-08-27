package ch.loewenfels.issuetrackingsync.executor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MapperFactoryTest {
    @Test
    fun getMapper_validClassName_instanceLoaded() {
        // arrange
        val directMapperName = DirectFieldMapper::class.qualifiedName ?: ""
        // act
        val mapperInstance = MapperFactory.getMapper(directMapperName)
        // assert
        assertNotNull(mapperInstance);
    }

    @Test
    fun getMapper_multipleCallsForSameClassName_sameInstance() {
        // arrange
        val directMapperName = DirectFieldMapper::class.qualifiedName ?: ""
        // act
        val mapperInstance1 = MapperFactory.getMapper(directMapperName)
        val mapperInstance2 = MapperFactory.getMapper(directMapperName)
        // assert
        assertSame(mapperInstance1, mapperInstance2, "Returned instances should be the same object");
    }

    @Test
    fun getMapper_invalidClassName_exception() {
        // arrange
        val directMapperName = "ch.loewenfels.MapperDoesntExist"
        // act
        assertThrows(IllegalArgumentException::class.java) { MapperFactory.getMapper(directMapperName) }
    }
}