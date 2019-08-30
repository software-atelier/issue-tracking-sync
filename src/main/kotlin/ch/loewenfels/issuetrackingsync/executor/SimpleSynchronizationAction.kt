package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue

class SimpleSynchronizationAction : AbstractSynchronizationAction(), SynchronizationAction {
    override fun execute(
        sourceClient: IssueTrackingClient<Any>,
        targetClient: IssueTrackingClient<Any>,
        issue: Issue,
        fieldMappings: List<FieldMapping>,
        defaultsForNewIssue: DefaultsForNewIssue?
    ) {
        buildTargetIssueValues(sourceClient, issue, fieldMappings)
        createOrUpdateTargetIssue(targetClient, issue, defaultsForNewIssue)
    }
}