package ch.loewenfels.issuetrackingsync.syncclient.jira

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * These tests rely on a valid JIRA setup. To run, remove the @Disabled and edit buildSetup()
 */
@Disabled
internal class JiraClientTest : AbstractSpringTest() {
    @Test
    fun getIssue_validKey_issueFound() {
        // arrange
        val testee = JiraClient(buildSetup());
        verifySetup(testee)
        // act
        val issue = testee.getIssue("DEV-44692")
        // assert
        assertNotNull(issue)
        assertEquals("DEV-44692", issue?.key)
    }

    @Test
    fun changedIssuesSince_updatedTwoDaysAgo_issuesCollectionNotNull() {
        // arrange
        val testee = JiraClient(buildSetup());
        verifySetup(testee)
        val lastUpdated = LocalDateTime.now().minusDays(2)
        // act
        val issues = testee.changedIssuesSince(lastUpdated)
        // assert
        assertNotNull(issues)
    }

    private fun buildSetup(): IssueTrackingApplication {
        return IssueTrackingApplication(
            "ch.loewenfels.issuetrackingsync.client.jira.JiraClient",
            "JIRA",
            "myusername",
            "mysecret",
            "https://jira.foobar.com/jira",
            false
        )
    }

    private fun verifySetup(client: JiraClient) {
        try {
            val greeting = client.verifySetup()
            Assumptions.assumeTrue(greeting.isNotEmpty())
        } catch (ex: Exception) {
            Assumptions.assumeTrue(false)
        }
    }
}