package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.any
import ch.loewenfels.issuetrackingsync.safeEq
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.syncclient.rtc.RtcClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import com.atlassian.jira.rest.client.api.domain.Status
import com.ibm.team.workitem.common.model.IDeliverable
import com.ibm.team.workitem.common.model.IWorkItem
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import com.atlassian.jira.rest.client.api.domain.Issue as AtlassianIssue


internal class FieldTargetVersionMapperTest : AbstractSpringTest() {


    @Test
    fun setValueForRtc_versionFoundAndSolved_setCorrectValue() {
        // arrange
        val iteration1 = trainDeliverable("I2001.1 - 2.3")
        val iteration2 = trainDeliverable("I2001.1 - 3.66.3")
        val targetClient = mock(RtcClient::class.java)
        `when`(targetClient.getValue(any(), any())).thenReturn("I2001.1 - 3.67")
        `when`(targetClient.getAllDeliverables()).thenReturn(listOf(iteration1, iteration2))
        val issue = createRtcIssue()
        issue.proprietarySourceInstance = trainJiraIssueWithStatus("erledigt")
        val testee = createTestee()
        // act
        testee.setValue(issue, "target", issue, targetClient, listOf("3.66", "3.66.1", "3.66.3"))
        // assert
        verify(targetClient)
            .setValue(safeEq(issue), safeEq(issue), safeEq("target"), safeEq("I2001.1 - 3.66.3"))
    }

    @Test
    fun setValueForRtc_versionFoundAndSolvedBugFixVersionIsHighest_setCorrectValue() {
        // arrange
        val iteration1 = trainDeliverable("I2001.1 - 2.3")
        val iteration2 = trainDeliverable("I2001.1 - 3.66.3")
        val targetClient = mock(RtcClient::class.java)
        `when`(targetClient.getValue(any(), any())).thenReturn("I2001.1 - 3.67")
        `when`(targetClient.getAllDeliverables()).thenReturn(listOf(iteration1, iteration2))
        val issue = createRtcIssue()
        issue.proprietarySourceInstance = trainJiraIssueWithStatus("erledigt")
        val testee = createTestee()
        // act
        testee.setValue(issue, "target", issue, targetClient, listOf("3.71", "3.66.1", "3.66.3"))
        // assert
        verify(targetClient)
            .setValue(safeEq(issue), safeEq(issue), safeEq("target"), safeEq("I2001.1 - 3.66.3"))
    }


    @Test
    fun setValueForRtc_versionNotMappable_expectIllegalStateException() {
        // arrange
        val iteration1 = trainDeliverable("I2001.1 - 2.3")
        val iteration2 = trainDeliverable("I2001.1 - 3.66.1")
        val targetClient = mock(RtcClient::class.java)
        `when`(targetClient.getValue(any(), any())).thenReturn("I2001.1 - 3.67")
        `when`(targetClient.getAllDeliverables()).thenReturn(listOf(iteration1, iteration2))
        val issue = createRtcIssue()
        issue.proprietarySourceInstance = trainJiraIssueWithStatus("erledigt")
        val testee = createTestee()
        // act + assert
        assertThrows<IllegalStateException> {
            testee.setValue(issue, "target", issue, targetClient, listOf("3.71.1.nil"))
        }
    }

    @Test
    fun setValueForRtc_versionNotFoundEmptyString_noValueSet() {
        // arrange
        val iteration1 = trainDeliverable("I2001.1 - 2.3")
        val iteration2 = trainDeliverable("I2001.1 - 3.66.1")
        val targetClient = mock(RtcClient::class.java)
        `when`(targetClient.getValue(any(), any())).thenReturn("I2001.1 - 3.67")
        `when`(targetClient.getAllDeliverables()).thenReturn(listOf(iteration1, iteration2))
        val issue = createRtcIssue()
        issue.proprietarySourceInstance = trainJiraIssueWithStatus("erledigt")
        val testee = createTestee()
        // act
        testee.setValue(issue, "target", issue, targetClient, listOf(""))
        // assert
        verify(targetClient, times(0))
            .setValue(safeEq(issue), safeEq(issue), safeEq("target"), safeEq("I2001.1 - 3.66.1"))
    }

    @Test
    fun setValueForRtc_versionFoundButNotSolved_valueNotSet() {
        // arrange
        val targetClient = mock(RtcClient::class.java)
        val issue = createRtcIssue()
        issue.proprietarySourceInstance = trainJiraIssueWithStatus("open")
        val testee = createTestee()
        // act
        assertThrows<IllegalStateException> {
            testee.setValue(issue, "target", issue, targetClient, listOf("3.66", "3.66.1", "3.66.3"))
        }
        // assert
        verify(targetClient, never()).setValue(any(), any(), any(), any())
    }

    @Test
    fun setValueForRtc_noVersionSetInJiraAndRtc_valueNotSet() {
        // arrange
        val targetClient = mock(RtcClient::class.java)
        val issue = createRtcIssue()
        val testee = createTestee()
        // act
        testee.setValue(issue, "target", issue, targetClient, listOf<String>())
        // assert
        verify(targetClient, never()).setValue(any(), any(), any(), any())
    }

    @Test
    fun getValueFromRtc_rtcValueIsBacklog_exception() {
        // arrange
        val targetClient = mock(RtcClient::class.java)
        `when`(targetClient.getValue(any(), any())).thenReturn("Backlog")
        val issue = createRtcIssue()
        val testee = createTestee()
        // act + assert (expected exception)
        assertThrows<IllegalStateException> {
            testee.getValue(issue.proprietaryTargetInstance as IWorkItem, "target", targetClient)
        }
    }

    @Test
    fun getValueFromRtc_rtcValueIsSpecificValueWithRegexForIt_returnTransformedString() {
        // arrange
        val targetClient = mock(RtcClient::class.java)
        `when`(targetClient.getValue(any(), any())).thenReturn("I2001.1 - 3.66")
        val issue = createRtcIssue()
        val testee = createTestee("I{1}\\d{4}\\.{1}\\d{1} - (\\d{1}\\.\\d{2})" to "$1")
        // act
        val actualValue =
            testee.getValue(issue.proprietaryTargetInstance as IWorkItem, "target", targetClient)
        // assert
        Assertions.assertEquals("3.66", actualValue)
    }

    @Test
    fun getValueFromRtc_rtcValueIsBacklogWithRegexForIt_noException() {
        // arrange
        val targetClient = mock(RtcClient::class.java)
        `when`(targetClient.getValue(any(), any())).thenReturn("Backlog")
        val issue = createRtcIssue()
        val testee = createTestee(
            "I{1}\\d{4}\\.{1}\\d{1} - (\\d{1}\\.\\d{2})" to "Test $1",
            "Backlog" to ""
        )
        // act
        val actualValue =
            testee.getValue(issue.proprietaryTargetInstance as IWorkItem, "target", targetClient)
        // assert
        Assertions.assertNull(actualValue)
    }

    @Test
    fun setValueForJira_rtcValueIsBacklog_exception() {
        // arrange
        val targetClient = mock(JiraClient::class.java)
        `when`(targetClient.getMultiSelectValues(any(), any())).thenReturn(listOf("3.67", "3.88", "3.12"))
        val issue = createJiraIssue()
        val testee = createTestee()
        // act + assert (expected exception)
        assertThrows<IllegalStateException> {
            testee.setValue(issue, "target", issue, targetClient, "Backlog")
        }
    }

    @Test
    fun setValueForJira_validRtcVersion_addVersion() {
        // arrange
        val targetClient = mock(JiraClient::class.java)
        `when`(targetClient.getMultiSelectValues(any(), any())).thenReturn(listOf("3.67", "3.88", "3.12"))
        val issue = createJiraIssue()
        val testee = createTestee()
        // act
        testee.setValue(issue, "target", issue, targetClient, "3.66")
        // assert
        val argumentCaptor = ArgumentCaptor.forClass(List::class.java)
        verify(targetClient)
            .setValue(safeEq(issue), safeEq(issue), safeEq("target"), argumentCaptor.capture())
        @Suppress("UNCHECKED_CAST")
        assertThat(
            argumentCaptor.value as List<String>,
            hasItems("3.66", "3.67", "3.88", "3.12")
        )
    }

    @Test
    fun setValueForJira_jiraHasBacklogSetRtcHasValidVersion_BacklogShouldBeRemoved() {
        // arrange
        val targetClient = mock(JiraClient::class.java)
        `when`(targetClient.getMultiSelectValues(any(), any())).thenReturn(listOf("3.67", "3.88", "3.12", "Backlog"))
        val issue = createJiraIssue()
        val testee = createTestee()
        // act
        testee.setValue(issue, "target", issue, targetClient, "3.66")
        // assert
        val argumentCaptor = ArgumentCaptor.forClass(List::class.java)
        verify(targetClient)
            .setValue(safeEq(issue), safeEq(issue), safeEq("target"), argumentCaptor.capture())
        @Suppress("UNCHECKED_CAST")
        assertThat(
            argumentCaptor.value as List<String>,
            hasItems("3.66", "3.67", "3.88", "3.12")
        )
    }

    @Test
    fun setValueForJira_jiraHasBacklogSetRtcHasBacklog_BacklogShouldNotBeRemoved() {
        // arrange
        val targetClient = mock(JiraClient::class.java)
        `when`(targetClient.getMultiSelectValues(any(), any())).thenReturn(listOf("3.67", "3.88", "3.12"))
        val issue = createJiraIssue()
        val testee = createTestee(
            "I{1}\\d{4}\\.{1}\\d{1} - (\\d{1}\\.\\d{2})" to "Test $1",
            "(Backlog-?F?C?B?)" to "$1"
        )
        // act
        testee.setValue(issue, "target", issue, targetClient, "Backlog")
        // assert
        val argumentCaptor = ArgumentCaptor.forClass(List::class.java)
        verify(targetClient)
            .setValue(safeEq(issue), safeEq(issue), safeEq("target"), argumentCaptor.capture())
        @Suppress("UNCHECKED_CAST")
        assertThat(
            argumentCaptor.value as List<String>,
            hasItems("3.67", "3.88", "3.12", "Backlog")
        )
    }

    private fun createTestee(
        vararg associations: Pair<String, String> =
            arrayOf("I{1}\\d{4}\\.{1}\\d{1} - (\\d{1}\\.\\d{2})" to "Test $1")
    ): FieldTargetVersionMapper {
        val fieldDefinition = FieldMappingDefinition(
            associations = mutableMapOf(*associations)
        )
        return FieldTargetVersionMapper(fieldDefinition)
    }

    private fun createJiraIssue(): Issue {
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        issue.proprietaryTargetInstance = mock(AtlassianIssue::class.java)
        return issue
    }

    private fun trainDeliverable(name: String): IDeliverable {
        val deliverable = mock(IDeliverable::class.java)
        `when`(deliverable.name).thenReturn(name)
        return deliverable
    }

    private fun createRtcIssue(): Issue {
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        issue.proprietaryTargetInstance = mock(IWorkItem::class.java)
        return issue
    }

    private fun trainJiraIssueWithStatus(statusName: String): AtlassianIssue {
        val jiraIssue = mock(AtlassianIssue::class.java)
        val status = mock(Status::class.java)
        `when`(status.name).thenReturn(statusName)
        `when`(jiraIssue.status).thenReturn(status)
        return jiraIssue
    }
}