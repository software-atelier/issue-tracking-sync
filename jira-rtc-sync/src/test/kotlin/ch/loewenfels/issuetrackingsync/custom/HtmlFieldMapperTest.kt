package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects.buildIssue
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects.buildIssueTrackingApplication
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects.buildIssueTrackingClient
import com.atlassian.jira.rest.client.api.domain.Issue
import com.ibm.team.foundation.common.text.XMLString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired

internal class HtmlFieldMapperTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory

    @Test
    fun getValue_readFromJiraCallsHtmlRendering() {
        // arrange
        val testee = HtmlFieldMapper()
        val issue = mock(Issue::class.java)
        val sourceClient = mock(JiraClient::class.java)
        `when`(sourceClient.getHtmlValue(issue, "description")).thenReturn("<h4>My HTML</h4>")
        // act
        val result = testee.getValue(issue, "description", sourceClient)
        // assert
        assertEquals("<h4>My HTML</h4>", result)
    }

    @Test
    fun setValue_convertRtcXhtmlToJiraWiki() {
        // arrange
        val testee = HtmlFieldMapper()
        val issue = buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        val targetClient = buildIssueTrackingClient(buildIssueTrackingApplication("JiraClient"), clientFactory)
        val value = XMLString.createFromXMLText("<p>This is <strong>really</strong> important!</p>")
        // act
        testee.setValue(issue, "summary", issue, targetClient, value)
        // assert
        verify(targetClient)
            .setHtmlValue(issue, issue, "summary", "<p>This is <strong>really</strong> important!</p>")
    }
}