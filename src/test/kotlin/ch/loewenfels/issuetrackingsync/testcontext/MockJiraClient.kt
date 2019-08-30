package ch.loewenfels.issuetrackingsync.testcontext

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

open class MockJiraClient(val setup: IssueTrackingApplication) : IssueTrackingClient<Issue> {
    private val testIssues = mutableListOf(
        Issue("MK-1", setup.name, LocalDateTime.now().minusHours(2)),//
        Issue("MK-2", setup.name, LocalDateTime.now().minusHours(3)),//
        Issue("MK-3", setup.name, LocalDateTime.now().minusHours(4))
    )

    override fun getIssue(key: String): Issue? {
        return testIssues.find { it.key == key }
    }

    override fun getIssueFromWebhookBody(body: JsonNode): Issue {
        val formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][xxx][xx][X]")
        return Issue(
            body.get("issue")?.get("key")?.asText() ?: "",
            setup.name,
            OffsetDateTime.parse(
                body.get("issue")?.get("fields")?.get("updated")?.asText() ?: "",
                formatter
            ).toLocalDateTime()
        )
    }

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
        return when (fieldName) {
            "priorityId" -> "12"
            "comments" -> "h4. Important stuff"
            else -> "foobar"
        }
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