package ch.loewenfels.issuetrackingsync.testcontext

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime

open class MockRtcClient(val setup: IssueTrackingApplication) : IssueTrackingClient<Issue> {
    private val testIssues = mutableListOf(
        Issue("1234", setup.name, LocalDateTime.now().minusHours(2)),//
        Issue("2345", setup.name, LocalDateTime.now().minusHours(3)),//
        Issue("3456", setup.name, LocalDateTime.now().minusHours(4))
    )

    override fun getIssue(key: String): Issue? {
        return testIssues.find { it.key == key }
    }

    override fun getIssueFromWebhookBody(body: JsonNode): Issue =
        throw UnsupportedOperationException("RTC does not support webhooks")

    override fun getProprietaryIssue(issueKey: String): Issue? {
        return getIssue(issueKey)
    }

    override fun getProprietaryIssue(fieldName: String, fieldValue: String): Issue? {
        return getIssue(fieldValue)
    }

    override fun getLastUpdated(internalIssue: Issue): LocalDateTime {
        return internalIssue.lastUpdated
    }

    override fun getValue(internalIssue: Issue, fieldName: String): Any? {
        return "foobar"
    }

    override fun setValue(internalIssueBuilder: Any, fieldName: String, value: Any?) {
    }

    override fun createOrUpdateTargetIssue(
        issue: Issue,
        defaultsForNewIssue: DefaultsForNewIssue?
    ) {
        testIssues.add(issue)
    }

    override fun changedIssuesSince(lastPollingTimestamp: LocalDateTime): Collection<Issue> {
        return testIssues
    }
}