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
class NotClosedChangeFilter : IssueFilter {
    private val closedJiraStatus = listOf("Resolved", "Closed")
    private val closedRtcStatus = listOf("ch.igs.team.workitem.workflow.change.state.s17")

    override fun test(client: IssueTrackingClient<out Any>, issue: Issue): Boolean {
        return when (client) {
            is RtcClient -> testNotClosedInRtc(client, issue)
            is JiraClient -> testNotClosedInJira(client, issue)
            // could also throw an exception here, but returning 'true' makes testing with mock clients a bit easier
            else -> true
        }
    }

    private fun testNotClosedInJira(client: JiraClient, issue: Issue): Boolean {
        val internalIssue = client.getProprietaryIssue(issue.key) as com.atlassian.jira.rest.client.api.domain.Issue
        val issueTyp = client.getValue(internalIssue, "issueType.name")
        val status = client.getValue(internalIssue, "status.name")
        return "IGS Change" == issueTyp && !closedJiraStatus.contains(status)
    }

    private fun testNotClosedInRtc(client: RtcClient, issue: Issue): Boolean {
        val internalIssue = client.getProprietaryIssue(issue.key) as IWorkItem
        val issueTyp = client.getValue(internalIssue, "workItemType")
        val status = client.getValue(internalIssue, "state2.stringIdentifier")
        return "ch.igs.team.apt.workItemType.change" == issueTyp && !closedRtcStatus.contains(status)
    }
}