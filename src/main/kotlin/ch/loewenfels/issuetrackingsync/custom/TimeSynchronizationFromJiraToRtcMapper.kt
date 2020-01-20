package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.fields.TimeFieldMapper
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import com.ibm.team.workitem.common.model.IWorkItem

class TimeSynchronizationFromJiraToRtcMapper : TimeFieldMapper() {

    override fun <T> getValue(
        proprietaryIssue: T,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>
    ): Number {
        return fieldname.split(",").stream()
            .map { super.getValue(proprietaryIssue, it, issueTrackingClient) }
            .map { it ?: 0 }
            .mapToLong(Number::toLong)
            .sum()
    }


    override
    fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        val splittedFieldName = fieldname.split(",")
        when (splittedFieldName.size) {
            2 -> {
                val newEstimatedTime = value as Number
                issueTrackingClient as IssueTrackingClient<IWorkItem>
                proprietaryIssueBuilder as IWorkItem
                val originalEstimate =
                    issueTrackingClient.getTimeValueInMinutes(proprietaryIssueBuilder, splittedFieldName[0])
                val oldCorrectedEstimatedTime =
                    issueTrackingClient.getTimeValueInMinutes(proprietaryIssueBuilder, splittedFieldName[1])
                if (newEstimatedTime.toInt() != oldCorrectedEstimatedTime.toInt()
                    && (originalEstimate.toInt() != newEstimatedTime.toInt() || oldCorrectedEstimatedTime.toInt() != 0)
                ) {
                    issueTrackingClient.setTimeValue(proprietaryIssueBuilder, issue, splittedFieldName[1], value)
                }

            }
        }
    }


}
