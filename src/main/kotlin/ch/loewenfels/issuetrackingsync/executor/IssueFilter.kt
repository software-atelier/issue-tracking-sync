package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient

interface IssueFilter {
    fun test(client: IssueTrackingClient<out Any>, issue: Issue): Boolean
}