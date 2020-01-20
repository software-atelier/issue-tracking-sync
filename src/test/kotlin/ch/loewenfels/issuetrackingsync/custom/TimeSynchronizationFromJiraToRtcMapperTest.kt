package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.executor.fields.TimeFieldMapper
import ch.loewenfels.issuetrackingsync.safeEq
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.syncclient.rtc.RtcClient
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired

internal class TimeSynchronizationFromJiraToRtcMapperTest : AbstractSpringTest() {

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
        Mockito.verify(targetClient).setTimeValue(safeEq(issue), safeEq(issue), safeEq(fieldName), safeEq(value))
    }

    @Test
    fun setValueRtc() {
        // arrange
        val testee = TimeSynchronizationFromJiraToRtcMapper()
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        val targetClient = Mockito.mock(RtcClient::class.java)
        val plannedValue = 15
        Mockito.`when`(targetClient.getTimeValueInMinutes(any(), any())).thenReturn(plannedValue)
        val value = 45 as Number
        val expectedValue = value
        val field1 = "field1"
        val field2 = "field2"
        val fieldname = "${field1},${field2}"
        // act
        testee.setValue(issue, fieldname, issue, targetClient, value)
        // assert
        Mockito.verify(targetClient).setTimeValue(safeEq(issue), safeEq(issue), safeEq(field2), safeEq(value))
    }
}