package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.syncclient.rtc.RtcClient
import ch.loewenfels.issuetrackingsync.syncconfig.SyncFlowDefinition
import com.ibm.team.workitem.common.model.IWorkItem
import com.atlassian.jira.rest.client.api.domain.Issue as JiraRestClientIssue


/**
 * This is an example of a custom class, ie. a class where configuration elements are coded instead of defined in a
 * JSON file. This implementation is specific to a certain JIRA / RTC setup, eeg. by using given internal state IDs
 */
class UnclosedDefectCreationDateWithRtcReferenceFilter : UnclosedDefectCreationDateFilter() {

    override fun testUnclosedIssueInJira(
        client: JiraClient,
        issue: Issue,
        syncFlowDefinition: SyncFlowDefinition
    ): Boolean {
        val internalIssue = client.getProprietaryIssue(issue.key) as JiraRestClientIssue
        val issueReference = client.getValue(
            internalIssue, syncFlowDefinition.writeBackFieldMappingDefinition[0].targetName
        )
        return super.testUnclosedIssueInJira(client, issue, syncFlowDefinition) && issueReference != null
    }

    override fun testUnclosedIssueInRtc(
        client: RtcClient,
        issue: Issue,
        syncFlowDefinition: SyncFlowDefinition
    ): Boolean {
        val internalIssue = client.getProprietaryIssue(issue.key) as IWorkItem
        val issueReference = client.getValue(
            internalIssue, syncFlowDefinition.writeBackFieldMappingDefinition[0].targetName
        )
        return super.testUnclosedIssueInRtc(client, issue, syncFlowDefinition) && issueReference != null
    }
}