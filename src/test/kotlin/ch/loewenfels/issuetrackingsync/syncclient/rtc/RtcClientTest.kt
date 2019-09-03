package ch.loewenfels.issuetrackingsync.syncclient.rtc

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * These tests rely on a valid RTC setup. To run, remove the @Disabled and edit buildSetup()
 */
//@Disabled
internal class RtcClientTest : AbstractSpringTest() {
    @Test
    fun getIssue_validKey_issueFound() {
        // arrange
        val testee = RtcClient(buildSetup());
        // act
        val issue = testee.getIssue("53883")
        // assert
        assertNotNull(issue)
        assertEquals("53883", issue?.key)
    }

    @Test
    fun changedIssuesSince_updatedTwoDaysAgo_issuesCollectionNotNull() {
        // arrange
        val testee = RtcClient(buildSetup());
        val lastUpdated = LocalDateTime.now().minusDays(2)
        // act
        val issues = testee.changedIssuesSince(lastUpdated)
        // assert
        assertNotNull(issues)
        assertTrue(issues.size > 2)
    }

    @Test
    fun getComments_validKey_commentsLoaded() {
        // arrange
        val testee = RtcClient(buildSetup());
        val issue = testee.getProprietaryIssue("53883") ?: throw IllegalArgumentException("Unknown issue")
        // act
        val comments = testee.getComments(issue)
        // assert
        assertNotNull(comments)
        assertEquals(2, comments.size)
    }

    @Test
    fun getAttachments_validKey_attachmentsLoaded() {
        // arrange
        val testee = RtcClient(buildSetup());
        val issue = testee.getProprietaryIssue("53883") ?: throw IllegalArgumentException("Unknown issue")
        // act
        val attachments = testee.getAttachments(issue)
        // assert
        assertNotNull(attachments)
        assertEquals(1, attachments.size)
    }

    @Test
    fun listMetadata_sysout() {
        // arrange
        val testee = RtcClient(buildSetup());
        // act
        testee.listMetadata().forEach { attr ->
            println("${attr.identifier} = ${attr.displayName}")
        }
    }

    private fun buildSetup(): IssueTrackingApplication {
        return IssueTrackingApplication(
            "ch.loewenfels.issuetrackingsync.client.rtc.RtcClient",
            "RTC",
            "myusername",
            "mysecret",
            "https://rtc.foobar.com/rtc",
            false,
            "Playground Löwenfels (RTC)"
        )
    }
}