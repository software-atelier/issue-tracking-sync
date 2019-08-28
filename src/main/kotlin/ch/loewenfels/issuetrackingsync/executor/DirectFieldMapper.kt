package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient

class DirectFieldMapper : FieldMapper {
    override fun <T> getValue(
        proprietaryIssue: T,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>
    ): Any? {
        return issueTrackingClient.getValue(proprietaryIssue, fieldname)
    }

    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        issueTrackingClient.setValue(proprietaryIssueBuilder, fieldname, value)
    }
}