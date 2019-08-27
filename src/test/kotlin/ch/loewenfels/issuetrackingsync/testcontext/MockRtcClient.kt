package ch.loewenfels.issuetrackingsync.testcontext

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import java.time.LocalDateTime

class MockRtcClient(val setup: IssueTrackingApplication) : IssueTrackingClient {
    private val testIssues = listOf(
        Issue("1234", setup.name, LocalDateTime.now().minusHours(2)),//
        Issue("2345", setup.name, LocalDateTime.now().minusHours(3)),//
        Issue("3456", setup.name, LocalDateTime.now().minusHours(4))
    )

    override fun getIssue(key: String): Issue? {
        return testIssues.find { it.key == key }
    }

    override fun changedIssuesSince(lastPollingTimestamp: LocalDateTime): Collection<Issue> {
        return testIssues
    }
}