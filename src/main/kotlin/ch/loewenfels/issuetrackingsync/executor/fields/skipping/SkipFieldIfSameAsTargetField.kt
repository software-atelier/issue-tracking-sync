package ch.loewenfels.issuetrackingsync.executor.fields.skipping

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldSkippingEvaluatorDefinition

class SkipFieldIfSameAsTargetField(fieldSkippingEvaluatorDefinition: FieldSkippingEvaluatorDefinition) :
        FieldSkippingEvaluator(fieldSkippingEvaluatorDefinition) {
    val properties: Map<String, Any> = fieldSkippingEvaluatorDefinition.properties

    override fun <T> hasFieldToBeSkipped(
            issueClient: IssueTrackingClient<in T>,
            issueBuilder: Any,
            issue: Issue,
            fieldname: String,
            sourceValue: Any?
    ): Boolean {
        @Suppress("UNCHECKED_CAST")
        val propIssue = issue.proprietaryTargetInstance as? T
        val field = properties["field"]
        if (field is String) {
            val fieldValue: Any? = propIssue?.let { issueClient.getValue(it, field) }

            return sourceValue == fieldValue
        }
        error("This class expects a property 'field' as a compare target field")
    }
}