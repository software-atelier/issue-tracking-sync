package ch.loewenfels.issuetrackingsync.client;

import ch.loewenfels.issuetrackingsync.dto.Issue
import java.time.LocalDateTime

interface IssueTrackingClient {
    fun changedIssuesSince(lastPollingTimestamp: LocalDateTime): Collection<Issue>
}
