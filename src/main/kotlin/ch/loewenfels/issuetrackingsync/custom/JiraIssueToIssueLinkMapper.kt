package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.fields.FieldMapper
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import com.atlassian.jira.rest.client.api.domain.Issue as JiraIssue

class JiraIssueToIssueLinkMapper : FieldMapper {

    override fun <T> getValue(
        proprietaryIssue: T,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>
    ): Any? = null

    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) =
        issueTrackingClient.setValue(
            proprietaryIssueBuilder,
            issue,
            fieldname,
            (issue.proprietarySourceInstance as JiraIssue).key
        )
}