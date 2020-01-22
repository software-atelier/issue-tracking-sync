package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.safeEq
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired

internal class TimeFieldMapperTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory

    @Test
    fun setValue_jira() {
        // arrange
        val testee = TimeFieldMapper()
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        val targetClient = Mockito.mock(JiraClient::class.java)
        val value = 0 as Number
        val fieldName = "timeTracking"
        // act
        testee.setValue(issue, fieldName, issue, targetClient, value)
        // assert
        verify(targetClient).setTimeValue(safeEq(issue), safeEq(issue), safeEq(fieldName), safeEq(value))
    }
}