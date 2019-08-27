package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient

interface SynchronizationAction {
    fun execute(sourceClient: IssueTrackingClient, targetClient: IssueTrackingClient, issue: Issue)
}