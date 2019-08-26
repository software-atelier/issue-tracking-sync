package ch.loewenfels.issuetrackingsync.testcontext

import ch.loewenfels.issuetrackingsync.client.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.dto.Issue
import ch.loewenfels.issuetrackingsync.settings.IssueTrackingApplication
import java.time.LocalDateTime

class MockJiraClient(val setup: IssueTrackingApplication) : IssueTrackingClient {
    private val testIssues = listOf(
        Issue("MK-1", setup.name, LocalDateTime.now().minusHours(2)),//
        Issue("MK-2", setup.name, LocalDateTime.now().minusHours(3)),//
        Issue("MK-3", setup.name, LocalDateTime.now().minusHours(4))
    )

    override fun getIssue(key: String): Issue? {
        return testIssues.find { it.key == key }
    }

    override fun changedIssuesSince(lastPollingTimestamp: LocalDateTime): Collection<Issue> {
        return testIssues
    }
}