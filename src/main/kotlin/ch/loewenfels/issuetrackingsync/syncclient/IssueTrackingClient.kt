package ch.loewenfels.issuetrackingsync.syncclient;

import ch.loewenfels.issuetrackingsync.Issue
import java.time.LocalDateTime

interface IssueTrackingClient {
    fun getIssue(key: String): Issue?
    fun changedIssuesSince(lastPollingTimestamp: LocalDateTime): Collection<Issue>
}
