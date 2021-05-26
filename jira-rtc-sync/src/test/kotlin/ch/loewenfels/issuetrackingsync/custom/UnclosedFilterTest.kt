package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.any
import ch.loewenfels.issuetrackingsync.safeEq
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.syncclient.rtc.RtcClient
import ch.loewenfels.issuetrackingsync.syncconfig.SyncFlowDefinition
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import com.atlassian.jira.rest.client.api.domain.Issue
import com.ibm.team.workitem.common.model.IWorkItem
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

abstract class UnclosedFilterTest {

    abstract fun getIssueTypePassingFilterJira(): String

    abstract fun getIssueTypeNotPassingFilterJira(): String

    abstract fun getIssueTypePassingFilterRtc(): String

    abstract fun getIssueTypeNotPassingFilterRtc(): String

    abstract fun getUnclosedFilter(): UnclosedFilter

    @Test
    fun test_jiraClientOnOpenIssueCorrectIssueType_true() {
        // arrange
        val issue = TestObjects.buildIssue("MK-1")
        val internalIssue = mock(Issue::class.java)
        val jiraClient = mock(JiraClient::class.java)
        `when`(jiraClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        `when`(jiraClient.getValue(safeEq(internalIssue), safeEq("issueType.name"))).thenReturn(
            getIssueTypePassingFilterJira()
        )
        `when`(jiraClient.getValue(safeEq(internalIssue), safeEq("status.name"))).thenReturn("In Progress")
        // act
        val result = getUnclosedFilter().test(jiraClient, issue, SyncFlowDefinition())
        // assert
        assertTrue(result)
    }

    @Test
    fun test_jiraClientOnClosedIssueCorrectIssueType_false() {
        // arrange
        val issue = TestObjects.buildIssue("MK-1")
        val internalIssue = mock(Issue::class.java)
        val jiraClient = mock(JiraClient::class.java)
        `when`(jiraClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        `when`(jiraClient.getValue(safeEq(internalIssue), safeEq("issueType.name")))
            .thenReturn(getIssueTypePassingFilterJira())
        `when`(jiraClient.getValue(safeEq(internalIssue), safeEq("status.name"))).thenReturn("Closed")
        // act
        val result = getUnclosedFilter().test(jiraClient, issue, SyncFlowDefinition())
        // assert
        assertFalse(result)
    }

    @Test
    fun test_jiraClientOnOpenIssueIncorrectIssueType_false() {
        // arrange
        val issue = TestObjects.buildIssue("MK-1")
        val internalIssue = mock(Issue::class.java)
        val jiraClient = mock(JiraClient::class.java)
        `when`(jiraClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        `when`(jiraClient.getValue(safeEq(internalIssue), safeEq("issueType.name")))
            .thenReturn(getIssueTypeNotPassingFilterJira())
        `when`(jiraClient.getValue(safeEq(internalIssue), safeEq("status.name"))).thenReturn("In Progress")
        // act
        val result = getUnclosedFilter().test(jiraClient, issue, SyncFlowDefinition())
        // assert
        assertFalse(result)
    }

    @Test
    fun test_rtcClientOnOpenIssueCorrectIssueType_true() {
        // arrange
        val issue = TestObjects.buildIssue("123456")
        val internalIssue = mock(IWorkItem::class.java)
        val rtcClient = mock(RtcClient::class.java)
        `when`(rtcClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        `when`(rtcClient.getValue(safeEq(internalIssue), safeEq("workItemType")))
            .thenReturn(getIssueTypePassingFilterRtc())
        `when`(rtcClient.getValue(safeEq(internalIssue), safeEq("state2.stringIdentifier")))
            .thenReturn("ch.igs.team.workitem.workflow.change.state.s5")
        // act
        val result = getUnclosedFilter().test(rtcClient, issue, SyncFlowDefinition())
        // assert
        assertTrue(result)
    }

    @Test
    @Disabled
    fun test_rtcClientOnIssueCorrectIssueType_false() {
        // arrange
        val issue = TestObjects.buildIssue("123456")
        val internalIssue = mock(IWorkItem::class.java)
        val rtcClient = mock(RtcClient::class.java)
        `when`(rtcClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        `when`(rtcClient.getValue(safeEq(internalIssue), safeEq("workItemType")))
            .thenReturn(getIssueTypePassingFilterRtc())
        `when`(rtcClient.getValue(safeEq(internalIssue), safeEq("state2.stringIdentifier")))
            .thenReturn("ch.igs.team.workitem.workflow.change.state.s17")
        // act
        val result = getUnclosedFilter().test(rtcClient, issue, SyncFlowDefinition())
        // assert
        assertFalse(result)
    }

    @Test
    fun test_rtcClientOnOpenIssueIncorrectIssueType_false() {
        // arrange
        val issue = TestObjects.buildIssue("123456")
        val internalIssue = mock(IWorkItem::class.java)
        val rtcClient = mock(RtcClient::class.java)
        `when`(rtcClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        `when`(rtcClient.getValue(safeEq(internalIssue), safeEq("workItemType")))
            .thenReturn(getIssueTypeNotPassingFilterRtc())
        `when`(rtcClient.getValue(safeEq(internalIssue), safeEq("state2.stringIdentifier")))
            .thenReturn("ch.igs.team.workitem.workflow.defect.state.s5")
        // act
        val result = getUnclosedFilter().test(rtcClient, issue, SyncFlowDefinition())
        // assert
        assertFalse(result)
    }

    @Test
    fun test_rtcClientOnClosedIssueButResolutionToSyncAndCorrectIssueType_true() {
        // arrange
        val issue = TestObjects.buildIssue("123456")
        val internalIssue = mock(IWorkItem::class.java)
        val rtcClient = mock(RtcClient::class.java)
        `when`(rtcClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        `when`(rtcClient.getValue(safeEq(internalIssue), safeEq("workItemType")))
            .thenReturn(getIssueTypePassingFilterRtc())
        `when`(rtcClient.getValue(safeEq(internalIssue), safeEq("state2.stringIdentifier")))
            .thenReturn("ch.igs.team.workitem.workflow.change.state.s17")
        `when`(rtcClient.getValue(safeEq(internalIssue), safeEq("internalResolution")))
            .thenReturn("Duplikat")
        // act
        val result = getUnclosedFilter().test(rtcClient, issue, SyncFlowDefinition())
        // assert
        assertTrue(result)
    }
}