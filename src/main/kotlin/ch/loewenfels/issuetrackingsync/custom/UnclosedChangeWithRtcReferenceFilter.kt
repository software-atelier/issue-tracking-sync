package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.IssueFilter
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.syncconfig.SyncFlowDefinition

class UnclosedChangeWithRtcReferenceFilter(
    private val closedJiraStatus: List<String> = listOf("Resolved", "Closed", "geschlossen", "erledigt")
) : IssueFilter {

    override fun test(
        client: IssueTrackingClient<out Any>,
        issue: Issue,
        syncFlowDefinition: SyncFlowDefinition
    ): Boolean = (client is JiraClient) && testUnclosedIssueWithRefToRtc(client, issue, syncFlowDefinition)

    private fun testUnclosedIssueWithRefToRtc(
        client: JiraClient,
        issue: Issue,
        syncFlowDefinition: SyncFlowDefinition
    ): Boolean {
        val internalIssue = client.getProprietaryIssue(issue.key) as com.atlassian.jira.rest.client.api.domain.Issue
        val status = client.getValue(internalIssue, "status.name")
        val issueType = client.getValue(internalIssue, "issueType.name")
        val issueReference = client.getValue(
            internalIssue, syncFlowDefinition.writeBackFieldMappingDefinition[0].targetName
        )
        return !closedJiraStatus.contains(status) //
                && isAllowedIssueType(issueType, UnclosedChangeFilter().getAllowedJiraIssueTypes())//
                && issueReference != null
    }

    private fun isAllowedIssueType(issueType: Any?, allowedJiraIssueTypes: List<String>) =
        allowedJiraIssueTypes.isEmpty() || allowedJiraIssueTypes.contains(issueType)
}