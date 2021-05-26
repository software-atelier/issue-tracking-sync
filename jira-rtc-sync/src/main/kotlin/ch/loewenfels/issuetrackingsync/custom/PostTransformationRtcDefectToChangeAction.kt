package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.actions.SimpleSynchronizationAction
import ch.loewenfels.issuetrackingsync.executor.fields.FieldMapping
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue

class PostTransformationRtcDefectToChangeAction(actionName: String) : SimpleSynchronizationAction(actionName) {

    override fun execute(
            sourceClient: IssueTrackingClient<Any>,
            targetClient: IssueTrackingClient<Any>,
            issue: Issue,
            fieldMappings: List<FieldMapping>,
            defaultsForNewIssue: DefaultsForNewIssue?
    ) {
        if (null != issue.proprietaryOldTargetInstance) {
            val jiraIssue = issue.proprietaryTargetInstance as com.atlassian.jira.rest.client.api.domain.Issue
            val iterator = jiraIssue.issueLinks?.iterator()
            if (iterator != null) {
                if (iterator.hasNext()) {
                    val issueEpic = iterator.next()
                    val oldIssue  = issue.proprietaryOldTargetInstance as com.atlassian.jira.rest.client.api.domain.Issue
                    val issueClone = Issue(oldIssue.key, "", issue.lastUpdated)
                    issueClone.proprietarySourceInstance = targetClient.getProprietaryIssue(issueEpic.targetIssueKey)
                    issueClone.proprietaryTargetInstance = oldIssue
                    super.execute(sourceClient, targetClient, issueClone, fieldMappings, defaultsForNewIssue)
                }
            }
        }
    }
}