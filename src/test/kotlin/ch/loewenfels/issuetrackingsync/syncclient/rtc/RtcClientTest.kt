package ch.loewenfels.issuetrackingsync.syncclient.rtc

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.Attachment
import ch.loewenfels.issuetrackingsync.Comment
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * These tests rely on a valid RTC setup. To run, remove the @Disabled and edit buildSetup()
 * Further you have to create a valid RTC Issue
 * due the configurability of rtc there is still a chance some tests won't work.
 */
@Disabled
internal class RtcClientTest : AbstractSpringTest() {
    private val issueId = "????"

    @Test
    fun getIssue_validKey_issueFound() {
        // arrange
        val testee = RtcClient(buildSetup())
        // act
        val issue = testee.getIssue(issueId)
        // assert
        assertNotNull(issue)
        assertEquals(issueId, issue?.key)
    }

    @Test
    fun changedIssuesSince_updatedTwoDaysAgo_issuesCollectionNotNull() {
        // arrange
        val testee = RtcClient(buildSetup())
        val lastUpdated = LocalDateTime.now().minusDays(2)
        // act
        val issues = testee.changedIssuesSince(lastUpdated, 0, 50)
        // assert
        assertNotNull(issues)
        assertTrue(issues.size > 2)
    }

    @Test
    fun getComments_validKey_commentsLoaded() {
        // arrange
        val testee = RtcClient(buildSetup())
        val issue = testee.getProprietaryIssue(issueId) ?: throw IllegalArgumentException("Unknown issue")
        val previousExistingComments = testee.getComments(issue).size
        testee.addComment(issue, Comment("someAuthor", LocalDateTime.now(), "Some Content", "1234"))
        // act
        val comments = testee.getComments(issue)
        // assert
        assertNotNull(comments)
        assertEquals(1, comments.count() - previousExistingComments)
    }

    @Test
    fun getAttachments_validKey_attachmentsLoaded() {
        // arrange
        val testee = RtcClient(buildSetup())
        val issue = testee.getProprietaryIssue(issueId) ?: throw IllegalArgumentException("Unknown issue")
        val previousAttachments = testee.getAttachments(issue).size
        testee.addAttachment(issue, Attachment("newFile", ByteArray(1)))
        // act
        val attachments = testee.getAttachments(issue)
        // assert
        assertNotNull(attachments)
        assertEquals(1, attachments.size - previousAttachments)
    }

    @Test
    fun getValue_internalTags_tagsLoaded() {
        // arrange
        val testee = RtcClient(buildSetup())
        val issue = testee.getProprietaryIssue(issueId) ?: throw IllegalArgumentException("Unknown issue")
        // act
        val tags = testee.getValue(issue, "internalTags")
        // assert
        assertNotNull(tags)
    }

    @Test
    fun listMetadata_sysout() {
        // arrange
        val testee = RtcClient(buildSetup())
        // act
        testee.listMetadata().forEach { attr ->
            println("${attr.identifier} = ${attr.displayName}")
        }
    }

    private fun buildSetup(): IssueTrackingApplication {
        return IssueTrackingApplication(
            "ch.loewenfels.issuetrackingsync.syncclient.rtc.RtcClient",
            "RTC",
            "myusername",
            "mysecret",
            "https://rtc.foobar.com/rtc",
            "",
            false,
            "Playground Löwenfels (RTC)"
        )
    }
}