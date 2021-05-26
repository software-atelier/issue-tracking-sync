package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.fields.TimeFieldMapper
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient

class TimeSynchronizationFromJiraToRtcMapper : TimeFieldMapper() {

    override fun <T> getValue(
        proprietaryIssue: T,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>
    ): Number = fieldname.split(",")
        .mapNotNull { super.getValue(proprietaryIssue, it, issueTrackingClient) }
        .map(Number::toLong)
        .sum()

    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        val splitFieldName = fieldname.split(",")
        if (splitFieldName.size == 2) {
            val newEstimatedTime = value as Number
            val originalEstimate =
                issueTrackingClient.getTimeValueInMinutes(proprietaryIssueBuilder, splitFieldName[0])
            val oldCorrectedEstimatedTime =
                issueTrackingClient.getTimeValueInMinutes(proprietaryIssueBuilder, splitFieldName[1])
            if (newEstimatedTime.toInt() != oldCorrectedEstimatedTime.toInt()
                && (newEstimatedTime.toInt() != originalEstimate.toInt() || oldCorrectedEstimatedTime.toInt() != 0)
            ) {
                issueTrackingClient.setTimeValue(proprietaryIssueBuilder, issue, splitFieldName[1], value)
            }
        }
    }
}
