package ch.loewenfels.issuetrackingsync.syncclient.jira

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import java.time.LocalDateTime

/**
 * These tests rely on a valid JIRA setup. To run, remove the @Disabled and edit buildSetup()
 */
@Disabled
internal class JiraClientTest : AbstractSpringTest() {
    @Test
    fun getIssue_validKey_issueFound() {
        // arrange
        val testee = JiraClient(buildSetup())
        verifySetup(testee)
        // act
        val issue = testee.getIssue("DEV-44692")
        // assert
        assertNotNull(issue)
        assertEquals("DEV-44692", issue?.key)
    }

    @Test
    fun getValue_descriptionAsHtml_issueFound() {
        // arrange
        val testee = JiraClient(buildSetup())
        verifySetup(testee)
        val issue = testee.getProprietaryIssue("DEV-44692") ?: throw IllegalArgumentException("Unknown key")
        // act
        val html = testee.getHtmlValue(issue, "description")
        // assert
        assertNotNull(issue)
        assertThat(html, containsString("<h4>"))
    }

    @Test
    fun changedIssuesSince_updatedTwoDaysAgo_issuesCollectionNotNull() {
        // arrange
        val testee = JiraClient(buildSetup())
        verifySetup(testee)
        val lastUpdated = LocalDateTime.now().minusDays(2)
        // act
        val issues = testee.changedIssuesSince(lastUpdated)
        // assert
        assertNotNull(issues)
    }

    @Test
    fun listFields() {
        // arrange
        val testee = JiraClient(buildSetup())
        verifySetup(testee)
        // act
        testee.listFields()
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