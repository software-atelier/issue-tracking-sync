package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.safeEq
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import com.atlassian.jira.rest.client.api.domain.TimeTracking
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired

internal class TimeTrackingFieldMapperTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory

    @Test
    fun setValue_jira() {
        // arrange
        val testee = TimeTrackingFieldMapper()
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        val targetClient = Mockito.mock(JiraClient::class.java)
        val value = TimeTrackingFieldMapper.TimeTracking(0, 480, 0)
        // act
        testee.setValue(issue, "", issue, targetClient, value)
        // assert
        val captor = ArgumentCaptor.forClass(Any::class.java)
        verify(targetClient).setValue(safeEq(issue), safeEq(issue), safeEq("timeTracking"), captor.capture())
        val jiraTimeTracking = captor.value as TimeTracking
        assertNull(jiraTimeTracking.remainingEstimateMinutes)
    }
}