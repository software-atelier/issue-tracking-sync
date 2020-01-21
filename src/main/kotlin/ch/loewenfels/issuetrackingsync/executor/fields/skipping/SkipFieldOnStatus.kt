package ch.loewenfels.issuetrackingsync.executor.fields.skipping

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldSkippingEvaluatorDefinition

open class SkipFieldOnStatus(fieldSkippingEvaluatorDefinition: FieldSkippingEvaluatorDefinition) :
    FieldSkippingEvaluator(fieldSkippingEvaluatorDefinition) {
    override fun <T> hasFieldToBeSkipped(
        issueClient: IssueTrackingClient<in T>,
        issueBuilder: Any,
        issue: Issue,
        fieldname: String
    ): Boolean {
        val propIssue = issue.proprietaryTargetInstance as? T ?: null
        val status = propIssue?.let { issueClient.getState(it) } ?: ""
        return when (status) {
            "" -> false
            else -> fieldSkippingEvaluatorDefinition.properties["allowedStates"]?.split(",")?.contains(status)?.not()
                ?: false
        }
    }
}