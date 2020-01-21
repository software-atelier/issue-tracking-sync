package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.fields.FieldSkippingEvaluator
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldSkippingEvaluatorDefinition
import com.ibm.team.workitem.common.model.IWorkItem

open class SkipDurationFieldOnRtcInIllegalStatus(fieldSkippingEvaluatorDefinition: FieldSkippingEvaluatorDefinition) :
    FieldSkippingEvaluator(fieldSkippingEvaluatorDefinition) {
    override fun <T> hasFieldToBeSkipped(
        issueClient: IssueTrackingClient<in T>,
        issueBuilder: Any,
        issue: Issue,
        fieldname: String
    ): Boolean {
        issueClient as IssueTrackingClient<IWorkItem>
        issueBuilder as IWorkItem
        val status = issueClient.getValue(issueBuilder, "state2.stringIdentifier")
        return !(fieldSkippingEvaluatorDefinition.properties["allowedStates"]?.split(",")?.contains(status)
            ?: false)
    }
}