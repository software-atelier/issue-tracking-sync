package ch.loewenfels.issuetrackingsync.syncclient.rtc

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.syncconfig.ApplicationRole
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * These tests rely on a valid JIRA setup. To run, remove the @Disabled and edit buildSetup()
 */
@Disabled
internal class RtcClientTest : AbstractSpringTest() {
    @Test
    fun getIssue_validKey_issueFound() {
        // arrange
        val testee = RtcClient(buildSetup());
        // act
        val issue = testee.getIssue("60895")
        // assert
        assertNotNull(issue)
        assertEquals("60895", issue?.key)
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

    private fun buildSetup(): IssueTrackingApplication {
        return IssueTrackingApplication(
            "ch.loewenfels.issuetrackingsync.client.rtc.RtcClient",
            ApplicationRole.MASTER,
            "RTC",
            "myusername",
            "mysecret",
            "https://rtc.foobar.com/rtc",
            false,
            mapOf("JIRA" to "ch.loewenfels.team.workitem.attribute.external_refid"),
            "Playground Löwenfels (RTC)"
        )
    }
}