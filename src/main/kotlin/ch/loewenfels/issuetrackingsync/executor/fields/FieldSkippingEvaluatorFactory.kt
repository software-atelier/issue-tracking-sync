package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.Logging
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition

object FieldSkippingEvaluatorFactory : Logging {
    fun getEvaluators(fieldMappingDefinition: FieldMappingDefinition): MutableList<FieldSkippingEvaluator> {
        return fieldMappingDefinition.fieldSkipEvalutors
            .mapNotNull(this::createFieldSkippingEvaluator)
            .mapNotNull { instantianteClass(it, fieldMappingDefinition) }
            .toMutableList()
    }

    private fun createFieldSkippingEvaluator(fieldSkippingEvaluatorClassName: String): Class<FieldSkippingEvaluator>? {
        val clazz: Class<FieldSkippingEvaluator>?
        try {
            clazz = Class.forName(fieldSkippingEvaluatorClassName) as Class<FieldSkippingEvaluator>
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Failed to load FieldSkippingEvaluator class ${fieldSkippingEvaluatorClassName}",
                e
            )
        }
        return clazz
    }

    private fun instantianteClass(
        clazz: Class<FieldSkippingEvaluator>,
        fieldMappingDefinition: FieldMappingDefinition
    ): FieldSkippingEvaluator? {
        return try {
            clazz.getConstructor(FieldMappingDefinition::class.java).newInstance(fieldMappingDefinition)
        } catch (e: Exception) {
            null
        } ?: try {
            clazz.getDeclaredConstructor().newInstance()
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to instantiate evaluator class $clazz", e)
        }
    }
}