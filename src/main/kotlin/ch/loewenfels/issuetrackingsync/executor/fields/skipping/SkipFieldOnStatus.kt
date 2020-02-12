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
        @Suppress("UNCHECKED_CAST")
        val propIssue = issue.proprietaryTargetInstance as? T
        val status = propIssue?.let { issueClient.getState(it) } ?: ""
        if(status == "") {
            return false
        }
        return fieldSkippingEvaluatorDefinition.properties["allowedStates"]?.split(",")
            ?.contains(status)?.not()?: false
    }
}