package ch.loewenfels.issuetrackingsync.syncconfig

data class FieldSkippingEvaluatorDefinition(
    /**
     * FQN of the fieldSkippingEvaluator
     */
    var className: String = "",
    /**
     * The class can hold some Properties
     */
    var properties: Map<String, String> = mutableMapOf()


)
