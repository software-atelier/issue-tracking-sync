package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.IssueFilter
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.syncclient.rtc.RtcClient
import com.ibm.team.workitem.common.model.IWorkItem

/**
 * This is an example of a custom class, ie. a class where configuration elements are coded instead of defined in a
 * JSON file. This implementation is specific to a certain JIRA / RTC setup, eeg. by using given internal state IDs
 */
abstract class UnclosedFilter(
    private val closedRtcStatus: List<String> = listOf("ch.igs.team.workitem.workflow.change.state.s17"),
    private val closedJiraStatus: List<String> = listOf("Resolved", "Closed")
) :
    IssueFilter {

    override fun test(client: IssueTrackingClient<out Any>, issue: Issue): Boolean {
        return when (client) {
            is RtcClient -> testUnclosedIssueInRtc(client, issue)
            is JiraClient -> testUnclosedIssueInJira(client, issue)
            // could also throw an exception here, but returning 'true' makes testing with mock clients a bit easier
            else -> true
        }
    }

    private fun testUnclosedIssueInJira(client: JiraClient, issue: Issue): Boolean {
        val internalIssue = client.getProprietaryIssue(issue.key) as com.atlassian.jira.rest.client.api.domain.Issue
        val status = client.getValue(internalIssue, "status.name")
        val issueType = client.getValue(internalIssue, "issueType.name")
        return !closedJiraStatus.contains(status) && isAllowedIssueType(issueType, getAllowedJiraIssueTypes())
    }

    private fun isAllowedIssueType(issueType: Any?, allowedJiraIssueTypes: List<String>) =
        allowedJiraIssueTypes.isEmpty() || allowedJiraIssueTypes.contains(issueType)


    abstract fun getAllowedJiraIssueTypes(): List<String>

    private fun testUnclosedIssueInRtc(client: RtcClient, issue: Issue): Boolean {
        val internalIssue = client.getProprietaryIssue(issue.key) as IWorkItem
        val status = client.getValue(internalIssue, "state2.stringIdentifier")
        val issueType = client.getValue(internalIssue, "workItemType")
        return !closedRtcStatus.contains(status) && isAllowedIssueType(issueType, getAllowedRtcIssueTypes())
    }

    abstract fun getAllowedRtcIssueTypes(): List<String>
}