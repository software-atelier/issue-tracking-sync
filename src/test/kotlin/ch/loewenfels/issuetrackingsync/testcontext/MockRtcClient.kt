package ch.loewenfels.issuetrackingsync.testcontext

import ch.loewenfels.issuetrackingsync.Attachment
import ch.loewenfels.issuetrackingsync.Comment
import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.random.Random

open class MockRtcClient(private val setup: IssueTrackingApplication) : IssueTrackingClient<Issue> {
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

    override fun getKey(internalIssue: Issue): String =
        internalIssue.key

    override fun getIssueUrl(internalIssue: Issue): String =
        "${setup.endpoint}/web/projects/${setup.project}#action=com.ibm.team.workitem.viewWorkItem&id=${internalIssue.key}"
            .replace("//", "/")

    override fun getHtmlValue(internalIssue: Issue, fieldName: String): Any? = getValue(internalIssue, fieldName)
    override fun getValue(internalIssue: Issue, fieldName: String): Any? {
        return when (fieldName) {
            "severity" -> "com.ibm.team.workitem.common.model.ISeverity:severity.s2"
            "priority" -> "com.ibm.team.workitem.common.model.IPriority:priority.literal.I12"
            "comments" -> "<b>Important stuff</b>"
            "text" -> "text should have no title"
            else -> "foobar"
        }
    }

    override fun setValue(
        internalIssueBuilder: Any,
        issue: Issue,
        fieldName: String,
        value: Any?
    ) {
    }

    override fun setHtmlValue(internalIssueBuilder: Any, issue: Issue, fieldName: String, htmlString: String) {
        
    }

    override fun createOrUpdateTargetIssue(
        issue: Issue,
        defaultsForNewIssue: DefaultsForNewIssue?
    ) {
        val targetIssue = Issue((abs(Random.nextInt()) % 10000).toString(), "RTC", LocalDateTime.now())
        testIssues.add(targetIssue)
        issue.proprietaryTargetInstance = targetIssue
    }

    override fun changedIssuesSince(lastPollingTimestamp: LocalDateTime): Collection<Issue> {
        return testIssues
    }

    override fun getComments(internalIssue: Issue): List<Comment> =
        listOf(
            Comment(
                "Quiet Mary",
                LocalDateTime.now().minusHours(36),
                "I'll have to take a closer look at the logic here"
            ),
            Comment("Impatient Rick", LocalDateTime.now().minusHours(24), "Mary, this is rather urgent!")
        )

    override fun addComment(internalIssue: Issue, comment: Comment) {
        // no-op
    }

    override fun getAttachments(internalIssue: Issue): List<Attachment> =
        listOf(
            Attachment(
                "explanation.docx",
                "some other content here".toByteArray()
            )
        )

    override fun addAttachment(internalIssue: Issue, attachment: Attachment) {
        // no-op
    }
}