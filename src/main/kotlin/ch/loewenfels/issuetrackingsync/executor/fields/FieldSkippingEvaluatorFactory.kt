package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.Logging
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import ch.loewenfels.issuetrackingsync.syncconfig.FieldSkippingEvaluatorDefinition

object FieldSkippingEvaluatorFactory : Logging {
    fun getEvaluators(fieldMappingDefinition: FieldMappingDefinition): MutableList<FieldSkippingEvaluator> {
        return fieldMappingDefinition.fieldSkipEvalutors
            .mapNotNull(this::createFieldSkippingEvaluator)
            .toMutableList()
    }

    private fun createFieldSkippingEvaluator(fieldSkippingEvaluatorDefinition: FieldSkippingEvaluatorDefinition): FieldSkippingEvaluator? {
        val clazz: Class<FieldSkippingEvaluator>?
        try {
            clazz = Class.forName(fieldSkippingEvaluatorDefinition.className) as Class<FieldSkippingEvaluator>
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Failed to load FieldSkippingEvaluator class ${fieldSkippingEvaluatorDefinition.className}",
                e
            )
        }
        return instantianteClass(clazz, fieldSkippingEvaluatorDefinition)
    }

    private fun instantianteClass(
        clazz: Class<FieldSkippingEvaluator>,
        fieldSkippingEvaluatorDefinition: FieldSkippingEvaluatorDefinition
    ): FieldSkippingEvaluator? {
        return try {
            clazz.getConstructor(FieldSkippingEvaluatorDefinition::class.java)
                .newInstance(fieldSkippingEvaluatorDefinition)
        } catch (e: Exception) {
            null
        } ?: try {
            clazz.getDeclaredConstructor().newInstance()
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to instantiate evaluator class $clazz", e)
        }
    }
}