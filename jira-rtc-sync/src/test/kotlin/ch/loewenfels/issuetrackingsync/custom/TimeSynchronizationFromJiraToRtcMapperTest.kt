package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.any
import ch.loewenfels.issuetrackingsync.safeEq
import ch.loewenfels.issuetrackingsync.syncclient.rtc.RtcClient
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

internal class TimeSynchronizationFromJiraToRtcMapperTest : AbstractSpringTest() {

    @Test
    fun setValue_rtc() {
        // arrange
        val testee = TimeSynchronizationFromJiraToRtcMapper()
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        val targetClient = mock(RtcClient::class.java)
        val plannedValue = 15
        `when`(targetClient.getTimeValueInMinutes(any(), any()))
            .thenReturn(plannedValue)
        val value = 45 as Number
        val field1 = "field1"
        val field2 = "field2"
        val fieldname = "${field1},${field2}"
        // act
        testee.setValue(issue, fieldname, issue, targetClient, value)
        // assert
        verify(targetClient).setTimeValue(safeEq(issue), safeEq(issue), safeEq(field2), safeEq(value))
    }
}