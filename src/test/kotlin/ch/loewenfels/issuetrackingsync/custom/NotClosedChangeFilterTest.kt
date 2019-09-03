package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.any
import ch.loewenfels.issuetrackingsync.safeEq
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.syncclient.rtc.RtcClient
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import com.atlassian.jira.rest.client.api.domain.Issue
import com.ibm.team.workitem.common.model.IWorkItem
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito

internal class NotClosedChangeFilterTest {
    @Test
    fun test_jiraClientOnOpenChange_true() {
        // arrange
        val issue = TestObjects.buildIssue("MK-1")
        val internalIssue = Mockito.mock(Issue::class.java)
        val jiraClient = Mockito.mock(JiraClient::class.java)
        Mockito.`when`(jiraClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("issueType.name"))).thenReturn("IGS Change")
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("status.name"))).thenReturn("In Progress")
        // act
        val result = NotClosedChangeFilter().test(jiraClient, issue)
        // assert
        assertTrue(result)
    }

    @Test
    fun test_jiraClientOnClosedChange_false() {
        // arrange
        val issue = TestObjects.buildIssue("MK-1")
        val internalIssue = Mockito.mock(Issue::class.java)
        val jiraClient = Mockito.mock(JiraClient::class.java)
        Mockito.`when`(jiraClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("issueType.name"))).thenReturn("IGS Change")
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("status.name"))).thenReturn("Closed")
        // act
        val result = NotClosedChangeFilter().test(jiraClient, issue)
        // assert
        assertFalse(result)
    }

    @Test
    fun test_jiraClientOnOpenDefect_true() {
        // arrange
        val issue = TestObjects.buildIssue("MK-1")
        val internalIssue = Mockito.mock(Issue::class.java)
        val jiraClient = Mockito.mock(JiraClient::class.java)
        Mockito.`when`(jiraClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("issueType.name"))).thenReturn("Defect")
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("status.name"))).thenReturn("In Progress")
        // act
        val result = NotClosedChangeFilter().test(jiraClient, issue)
        // assert
        assertFalse(result)
    }

    @Test
    fun test_rtcClientOnOpenChange_true() {
        // arrange
        val issue = TestObjects.buildIssue("123456")
        val internalIssue = Mockito.mock(IWorkItem::class.java)
        val rtcClient = Mockito.mock(RtcClient::class.java)
        Mockito.`when`(rtcClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("workItemType")))
            .thenReturn("ch.igs.team.apt.workItemType.change")
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("state2.stringIdentifier")))
            .thenReturn("ch.igs.team.workitem.workflow.change.state.s5")
        // act
        val result = NotClosedChangeFilter().test(rtcClient, issue)
        // assert
        assertTrue(result)
    }

    @Test
    fun test_rtcClientOnClosedChange_false() {
        // arrange
        val issue = TestObjects.buildIssue("123456")
        val internalIssue = Mockito.mock(IWorkItem::class.java)
        val rtcClient = Mockito.mock(RtcClient::class.java)
        Mockito.`when`(rtcClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("workItemType")))
            .thenReturn("ch.igs.team.apt.workItemType.change")
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("state2.stringIdentifier")))
            .thenReturn("ch.igs.team.workitem.workflow.change.state.s17")
        // act
        val result = NotClosedChangeFilter().test(rtcClient, issue)
        // assert
        assertFalse(result)
    }

    @Test
    fun test_rtcClientOnOpenDefect_false() {
        // arrange
        val issue = TestObjects.buildIssue("123456")
        val internalIssue = Mockito.mock(IWorkItem::class.java)
        val rtcClient = Mockito.mock(RtcClient::class.java)
        Mockito.`when`(rtcClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("workItemType")))
            .thenReturn("ch.igs.team.apt.workItemType.defect")
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("state2.stringIdentifier")))
            .thenReturn("ch.igs.team.workitem.workflow.defect.state.s5")
        // act
        val result = NotClosedChangeFilter().test(rtcClient, issue)
        // assert
        assertFalse(result)
    }
}