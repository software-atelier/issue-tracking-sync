package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient

abstract class AbstractSynchronizationAction {
    protected fun loadProprietaryIssueInstances(
        sourceClient: IssueTrackingClient, targetClient: IssueTrackingClient, issue: Issue
    ) {
        issue.proprietarySourceInstance = sourceClient.getPropietaryIssue(issue)
        issue.proprietaryTargetInstance = targetClient.getPropietaryIssue(issue)
    }
}