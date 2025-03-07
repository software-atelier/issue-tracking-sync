package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient

open class DirectFieldMapper : FieldMapper {
  override fun <T> getValue(
    proprietaryIssue: T,
    fieldname: String,
    issueTrackingClient: IssueTrackingClient<in T>
  ): Any? = issueTrackingClient.getValue(proprietaryIssue, fieldname)

  override fun <T> setValue(
    proprietaryIssueBuilder: Any,
    fieldname: String,
    issue: Issue,
    issueTrackingClient: IssueTrackingClient<in T>,
    value: Any?
  ) {
    issueTrackingClient.setValue(proprietaryIssueBuilder, issue, fieldname, value)
  }
}