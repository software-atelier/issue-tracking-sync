package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.any
import ch.loewenfels.issuetrackingsync.safeEq
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.syncclient.rtc.RtcClient
import ch.loewenfels.issuetrackingsync.syncconfig.SyncFlowDefinition
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import com.atlassian.jira.rest.client.api.domain.Issue
import com.ibm.team.workitem.common.model.IWorkItem
import org.joda.time.DateTime
import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.sql.Timestamp
import java.time.LocalDateTime

internal abstract class UnclosedIssueCreationDateFilterTest : UnclosedFilterTest() {

    @Test
    fun test_jiraOnOpenIssueCorrectIssueTypeBeforeDateEquals_false() {
        // arrange
        val issue = TestObjects.buildIssue("MK-1")
        val internalIssue = Mockito.mock(Issue::class.java)
        val jiraClient = Mockito.mock(JiraClient::class.java)
        Mockito.`when`(jiraClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("issueType.name"))).thenReturn(
            getIssueTypePassingFilterJira()
        )
         Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("creationDate"))).thenReturn(
            DateTime.parse("2020-05-01T00:00")
        )
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("status.name"))).thenReturn("Closed")
        val unclosedFilter = getUnclosedFilter()
        unclosedFilter.defineParameters(mapOf("createdBefore" to "2020-05-01T00:00"))
        // act
        val result = unclosedFilter.test(jiraClient, issue, SyncFlowDefinition())
        // assert
        Assertions.assertFalse(result)
    }

    @Test
    fun test_jiraOnOpenIssueCorrectIssueTypeAfterDateEquals_true() {
        // arrange
        val issue = TestObjects.buildIssue("MK-1")
        val internalIssue = Mockito.mock(Issue::class.java)
        val jiraClient = Mockito.mock(JiraClient::class.java)
        Mockito.`when`(jiraClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("issueType.name"))).thenReturn(
            getIssueTypePassingFilterJira()
        )
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("status.name"))).thenReturn("In Progress")
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("creationDate"))).thenReturn(
            DateTime.parse("2020-05-01T00:00")
        )
        val unclosedFilter = getUnclosedFilter()
        unclosedFilter.defineParameters(mapOf("createdAfter" to "2020-05-01T00:00"))
        // act
        val result = unclosedFilter.test(jiraClient, issue, SyncFlowDefinition())
        // assert
        Assertions.assertTrue(result)
    }



    @Test
    fun test_jiraOnOpenIssueCorrectIssueTypeAfterKeyDateIsBeforeCreationDateOfIssue_true() {
        // arrange
        val issue = TestObjects.buildIssue("MK-1")
        val internalIssue = Mockito.mock(Issue::class.java)
        val jiraClient = Mockito.mock(JiraClient::class.java)
        Mockito.`when`(jiraClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("issueType.name"))).thenReturn(
            getIssueTypePassingFilterJira()
        )
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("status.name"))).thenReturn("In Progress")
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("creationDate"))).thenReturn(
            DateTime.parse("2020-05-01T00:00")
        )
        val unclosedFilter = getUnclosedFilter()
        unclosedFilter.defineParameters(mapOf("createdAfter" to "2020-01-01T00:00"))
        // act
        val result = unclosedFilter.test(jiraClient, issue, SyncFlowDefinition())
        // assert
        Assertions.assertTrue(result)
    }

    @Test
    fun test_jiraOnOpenIssueCorrectIssueTypeAfterKeyDateIsAfterCreationDateOfIssue_false() {
        // arrange
        val issue = TestObjects.buildIssue("MK-1")
        val internalIssue = Mockito.mock(Issue::class.java)
        val jiraClient = Mockito.mock(JiraClient::class.java)
        Mockito.`when`(jiraClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("issueType.name"))).thenReturn(
            getIssueTypePassingFilterJira()
        )
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("status.name"))).thenReturn("In Progress")
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("creationDate"))).thenReturn(
            DateTime.parse("2020-05-01T00:00")
        )
        val unclosedFilter = getUnclosedFilter()
        unclosedFilter.defineParameters(mapOf("createdAfter" to "2020-08-01T00:00"))
        // act
        val result = unclosedFilter.test(jiraClient, issue, SyncFlowDefinition())
        // assert
        Assertions.assertFalse(result)
    }

    @Test
    fun test_jiraOnOpenIssueCorrectIssueTypeBeforeKeyDateIsBeforeCreationDateOfIssue_false() {
        // arrange
        val issue = TestObjects.buildIssue("MK-1")
        val internalIssue = Mockito.mock(Issue::class.java)
        val jiraClient = Mockito.mock(JiraClient::class.java)
        Mockito.`when`(jiraClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("issueType.name"))).thenReturn(
            getIssueTypePassingFilterJira()
        )
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("status.name"))).thenReturn("In Progress")
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("creationDate"))).thenReturn(
            DateTime.parse("2020-05-01T00:00")
        )
        val unclosedFilter = getUnclosedFilter()
        unclosedFilter.defineParameters(mapOf("createdBefore" to "2020-01-01T00:00"))
        // act
        val result = unclosedFilter.test(jiraClient, issue, SyncFlowDefinition())
        // assert
        Assertions.assertFalse(result)
    }

    @Test
    fun test_jiraOnOpenIssueCorrectIssueTypeBeforeKeyDateIsAfterCreationDateOfIssue_true() {
        // arrange
        val issue = TestObjects.buildIssue("MK-1")
        val internalIssue = Mockito.mock(Issue::class.java)
        val jiraClient = Mockito.mock(JiraClient::class.java)
        Mockito.`when`(jiraClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("issueType.name"))).thenReturn(
            getIssueTypePassingFilterJira()
        )
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("status.name"))).thenReturn("In Progress")
        Mockito.`when`(jiraClient.getValue(safeEq(internalIssue), safeEq("creationDate"))).thenReturn(
            DateTime.parse("2020-05-01T00:00")
        )
        val unclosedFilter = getUnclosedFilter()
        unclosedFilter.defineParameters(mapOf("createdBefore" to "2020-08-01T00:00"))
        // act
        val result = unclosedFilter.test(jiraClient, issue, SyncFlowDefinition())
        // assert
        Assertions.assertTrue(result)
    }

    @Test
    fun test_rtcOnOpenIssueCorrectIssueTypeBeforeDateEquals_false() {
        // arrange
        val issue = TestObjects.buildIssue("123456")
        val internalIssue = Mockito.mock(IWorkItem::class.java)
        val rtcClient = Mockito.mock(RtcClient::class.java)
        Mockito.`when`(rtcClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("workItemType")))
            .thenReturn(getIssueTypePassingFilterRtc())
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("state2.stringIdentifier")))
            .thenReturn("ch.igs.team.workitem.workflow.change.state.s5")
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("creationDate"))).thenReturn(
            Timestamp.valueOf(LocalDateTime.of(2020,5,1,0,0))
        )
        val unclosedFilter = getUnclosedFilter()
        unclosedFilter.defineParameters(mapOf("createdBefore" to "2020-05-01T00:00"))
        // act
        val result = unclosedFilter.test(rtcClient, issue, SyncFlowDefinition())
        // assert
        Assertions.assertFalse(result)
    }

    @Test
    fun test_rtcOnOpenIssueCorrectIssueTypeAfterDateEquals_true() {
        // arrange
        val issue = TestObjects.buildIssue("123456")
        val internalIssue = Mockito.mock(IWorkItem::class.java)
        val rtcClient = Mockito.mock(RtcClient::class.java)
        Mockito.`when`(rtcClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("workItemType")))
            .thenReturn(getIssueTypePassingFilterRtc())
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("state2.stringIdentifier")))
            .thenReturn("ch.igs.team.workitem.workflow.change.state.s5")
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("creationDate"))).thenReturn(
            Timestamp.valueOf(LocalDateTime.of(2020,5,1,0,0))
        )
        val unclosedFilter = getUnclosedFilter()
        unclosedFilter.defineParameters(mapOf("createdAfter" to "2020-05-01T00:00"))
        // act
        val result = unclosedFilter.test(rtcClient, issue, SyncFlowDefinition())
        // assert
        Assertions.assertTrue(result)
    }

    @Test
    fun test_rtcOnOpenIssueCorrectIssueTypeAfterKeyDateIsBeforeCreationDateOfIssue_true() {
        // arrange
        val issue = TestObjects.buildIssue("123456")
        val internalIssue = Mockito.mock(IWorkItem::class.java)
        val rtcClient = Mockito.mock(RtcClient::class.java)
        Mockito.`when`(rtcClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("workItemType")))
            .thenReturn(getIssueTypePassingFilterRtc())
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("state2.stringIdentifier")))
            .thenReturn("ch.igs.team.workitem.workflow.change.state.s5")
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("creationDate"))).thenReturn(
            Timestamp.valueOf(LocalDateTime.of(2020,5,1,0,0))
        )
        val unclosedFilter = getUnclosedFilter()
        unclosedFilter.defineParameters(mapOf("createdAfter" to "2020-01-01T00:00"))
        // act
        val result = unclosedFilter.test(rtcClient, issue, SyncFlowDefinition())
        // assert
        Assertions.assertTrue(result)
    }

    @Test
    fun test_rtcOnOpenIssueCorrectIssueTypeAfterKeyDateIsAfterCreationDateOfIssue_false() {
        // arrange
        val issue = TestObjects.buildIssue("123456")
        val internalIssue = Mockito.mock(IWorkItem::class.java)
        val rtcClient = Mockito.mock(RtcClient::class.java)
        Mockito.`when`(rtcClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("workItemType")))
            .thenReturn(getIssueTypePassingFilterRtc())
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("state2.stringIdentifier")))
            .thenReturn("ch.igs.team.workitem.workflow.change.state.s5")
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("creationDate"))).thenReturn(
            Timestamp.valueOf(LocalDateTime.of(2020,5,1,0,0))
        )
        val unclosedFilter = getUnclosedFilter()
        unclosedFilter.defineParameters(mapOf("createdAfter" to "2020-08-01T00:00"))
        // act
        val result = unclosedFilter.test(rtcClient, issue, SyncFlowDefinition())
        // assert
        Assertions.assertFalse(result)
    }

    @Test
    fun test_rtcOnOpenIssueCorrectIssueTypeBeforeKeyDateIsBeforeCreationDateOfIssue_false() {
        // arrange
        val issue = TestObjects.buildIssue("123456")
        val internalIssue = Mockito.mock(IWorkItem::class.java)
        val rtcClient = Mockito.mock(RtcClient::class.java)
        Mockito.`when`(rtcClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("workItemType")))
            .thenReturn(getIssueTypePassingFilterRtc())
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("state2.stringIdentifier")))
            .thenReturn("ch.igs.team.workitem.workflow.change.state.s5")
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("creationDate"))).thenReturn(
            Timestamp.valueOf(LocalDateTime.of(2020,5,1,0,0))
        )
        val unclosedFilter = getUnclosedFilter()
        unclosedFilter.defineParameters(mapOf("createdBefore" to "2020-01-01T00:00"))
        // act
        val result = unclosedFilter.test(rtcClient, issue, SyncFlowDefinition())
        // assert
        Assertions.assertFalse(result)
    }

    @Test
    fun test_rtcOnOpenIssueCorrectIssueTypeBeforeKeyDateIsAfterCreationDateOfIssue_true() {
        // arrange
        val issue = TestObjects.buildIssue("123456")
        val internalIssue = Mockito.mock(IWorkItem::class.java)
        val rtcClient = Mockito.mock(RtcClient::class.java)
        Mockito.`when`(rtcClient.getProprietaryIssue(any(String::class.java))).thenReturn(internalIssue)
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("workItemType")))
            .thenReturn(getIssueTypePassingFilterRtc())
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("state2.stringIdentifier")))
            .thenReturn("ch.igs.team.workitem.workflow.change.state.s5")
        Mockito.`when`(rtcClient.getValue(safeEq(internalIssue), safeEq("createdBefore"))).thenReturn(
            Timestamp.valueOf(LocalDateTime.of(2020,5,1,0,0))
        )
        val unclosedFilter = getUnclosedFilter()
        unclosedFilter.defineParameters(mapOf("createdBefore" to "2020-08-01T00:00"))
        // act
        val result = unclosedFilter.test(rtcClient, issue, SyncFlowDefinition())
        // assert
        Assertions.assertTrue(result)
    }
}