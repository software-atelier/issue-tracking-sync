package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.actions.SimpleSynchronizationAction
import ch.loewenfels.issuetrackingsync.executor.fields.FieldMapping
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import com.atlassian.jira.rest.client.api.domain.Issue as JiraIssue

class TransformationRtcDefectToChangeAction(
    actionName: String
) : SimpleSynchronizationAction(actionName) {

    override fun execute(
        sourceClient: IssueTrackingClient<Any>,
        targetClient: IssueTrackingClient<Any>,
        issue: Issue,
        fieldMappings: List<FieldMapping>,
        defaultsForNewIssue: DefaultsForNewIssue?
    ) {
        val jiraClient = targetClient as JiraClient
        val jiraIssue = (issue.proprietaryTargetInstance ?: jiraClient.getProprietaryIssue(issue)) as JiraIssue?
        if ("Defekt" == jiraIssue?.issueType?.name) {
            // Remove references from JIRA Defect
            super.execute(sourceClient, targetClient, issue, fieldMappings, defaultsForNewIssue)
            // Remove target issue so new one can be created
            issue.proprietaryOldTargetInstance = issue.proprietaryTargetInstance
            issue.proprietaryTargetInstance = null
            issue.createNewOne = true
        }
    }
}