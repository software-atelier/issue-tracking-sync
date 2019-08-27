package ch.loewenfels.issuetrackingsync.executor

object MapperFactory {
    private val mapperInstances = mutableMapOf<String, FieldMapper>()
    fun getMapper(mapperClassname: String): FieldMapper {
        return try {
            mapperInstances.computeIfAbsent(mapperClassname) {
                val mapperClass = Class.forName(it) as Class<FieldMapper>
                mapperClass.newInstance()
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to load field mapper class " + mapperClassname, e)
        }
    }
}