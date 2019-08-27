package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient

class SyncChangesAction : AbstractSynchronizationAction(), SynchronizationAction {
    // TODO: receive fieldMapper directives (possibly in AbstractSynchronizationAction),
    // and process those
    // TODO: separate logic for create? (use if issue.proprietaryTargetInstance is empty)
    override fun execute(sourceClient: IssueTrackingClient, targetClient: IssueTrackingClient, issue: Issue) {
        loadProprietaryIssueInstances(sourceClient, targetClient, issue)
    }
}