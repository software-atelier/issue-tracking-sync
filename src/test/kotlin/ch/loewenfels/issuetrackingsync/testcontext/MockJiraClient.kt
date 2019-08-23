package ch.loewenfels.issuetrackingsync.testcontext

import ch.loewenfels.issuetrackingsync.client.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.dto.Issue
import ch.loewenfels.issuetrackingsync.settings.IssueTrackingApplication
import java.time.LocalDateTime

class MockJiraClient(val setup: IssueTrackingApplication) : IssueTrackingClient {
    override fun changedIssuesSince(lastPollingTimestamp: LocalDateTime): Collection<Issue> {
        return listOf(
            Issue("MK-1", setup.name, LocalDateTime.now().minusHours(2)),//
            Issue("MK-2", setup.name, LocalDateTime.now().minusHours(3)),//
            Issue("MK-3", setup.name, LocalDateTime.now().minusHours(4))
        )
    }
}