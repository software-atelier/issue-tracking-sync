package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.any
import ch.loewenfels.issuetrackingsync.safeEq
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.syncclient.rtc.RtcClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import com.ibm.team.process.common.IIteration
import com.ibm.team.workitem.common.model.IWorkItem
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.`when`


internal class FieldTargetVersionMapperTest : AbstractSpringTest() {
    @Test
    fun setValueForRtc() {
        // arrange
        val associations =
            mutableMapOf(
                "I{1}\\d{4}\\.{1}\\d{1} - (\\d{1}\\.\\d{2})" to "Test $1"
            )

        val fieldDefinition = FieldMappingDefinition(associations = associations)
        val testee = FieldTargetVersionMapper(fieldDefinition)
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        issue.proprietaryTargetInstance = Mockito.mock(IWorkItem::class.java)
        val targetClient = Mockito.mock(RtcClient::class.java)
        val value = "3.66"
        val fieldname = "target"
        val iteration = Mockito.mock(IIteration::class.java)
        val iteration2 = Mockito.mock(IIteration::class.java)
        `when`(iteration.name).thenReturn("I2001.1 - 2.3")
        `when`(iteration2.name).thenReturn("I2001.1 - 3.66")
        `when`(targetClient.getValue(any(), any())).thenReturn("I2001.1 - 3.67")
        `when`(targetClient.getAllIIteration()).thenReturn(listOf(iteration, iteration2))
        // act
        testee.setValue(issue, fieldname, issue, targetClient, listOf(value, "3.66.1", "3.66.3"))
        // assert
        Mockito.verify(targetClient)
            .setValue(safeEq(issue), safeEq(issue), safeEq(fieldname), safeEq("I2001.1 - 3.66"))
    }


    @Test
    fun getValueFromRtc_RTCValueIsBacklog_ExpectException() {
        // arrange
        val associations =
            mutableMapOf(
                "I{1}\\d{4}\\.{1}\\d{1} - (\\d{1}\\.\\d{2})" to "Test $1"
            )
        val fieldDefinition = FieldMappingDefinition(associations = associations)
        val testee = FieldTargetVersionMapper(fieldDefinition)
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        issue.proprietaryTargetInstance = Mockito.mock(IWorkItem::class.java)
        val targetClient = Mockito.mock(RtcClient::class.java)
        val fieldname = "target"
        `when`(targetClient.getValue(any(), any())).thenReturn("Backlog")
        // act + assert (expected exception)
        assertThrows<IllegalStateException> {
            testee.getValue(issue.proprietaryTargetInstance as IWorkItem, fieldname, targetClient)
        }
    }

    @Test

    fun getValueFromRtc_RTCValueIsSomeSpecificValueWithRegexForIt_transformedStringShouldGetReturned() {
        // arrange
        val associations =
            mutableMapOf(
                "I{1}\\d{4}\\.{1}\\d{1} - (\\d{1}\\.\\d{2})" to "$1"
            )
        val fieldDefinition = FieldMappingDefinition(associations = associations)
        val testee = FieldTargetVersionMapper(fieldDefinition)
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        issue.proprietaryTargetInstance = Mockito.mock(IWorkItem::class.java)
        val targetClient = Mockito.mock(RtcClient::class.java)
        val fieldname = "target"

        `when`(targetClient.getValue(any(), any())).thenReturn("I2001.1 - 3.66")
        // act
        val actualValue =
            testee.getValue(issue.proprietaryTargetInstance as IWorkItem, fieldname, targetClient)
        // assert
        Assertions.assertEquals("3.66", actualValue)
    }

    @Test
    fun getValueFromRtc_RTCValueIsBacklogWithRegexForIt_noException() {
        // arrange
        val associations =
            mutableMapOf(
                "I{1}\\d{4}\\.{1}\\d{1} - (\\d{1}\\.\\d{2})" to "Test $1",
                "Backlog" to ""
            )
        val fieldDefinition = FieldMappingDefinition(associations = associations)
        val testee = FieldTargetVersionMapper(fieldDefinition)
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        issue.proprietaryTargetInstance = Mockito.mock(IWorkItem::class.java)
        val targetClient = Mockito.mock(RtcClient::class.java)
        val fieldname = "target"
        `when`(targetClient.getValue(any(), any())).thenReturn("Backlog")
        // act
        val actualValue =
            testee.getValue(issue.proprietaryTargetInstance as IWorkItem, fieldname, targetClient)
        // assert
        Assertions.assertNull(actualValue)
    }

    @Test
    fun setValueForJira_RTCValueIsBacklog() {
        // arrange
        val associations =
            mutableMapOf(
                "I{1}\\d{4}\\.{1}\\d{1} - (\\d{1}\\.\\d{2})" to "Test $1"
            )
        val fieldDefinition = FieldMappingDefinition(associations = associations)
        val testee = FieldTargetVersionMapper(fieldDefinition)
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        issue.proprietaryTargetInstance = Mockito.mock(com.atlassian.jira.rest.client.api.domain.Issue::class.java)
        val targetClient = Mockito.mock(JiraClient::class.java)
        val value = "Backlog"
        val fieldname = "target"
        `when`(targetClient.getMultiSelectValues(any(), any())).thenReturn(listOf("3.67", "3.88", "3.12"))
        // act + assert (expected exception)
        assertThrows<IllegalStateException> {
            testee.setValue(issue, fieldname, issue, targetClient, value)
        }
    }

    @Test
    fun setValueForJira() {
        // arrange
        val associations =
            mutableMapOf(
                "I{1}\\d{4}\\.{1}\\d{1} - (\\d{1}\\.\\d{2})" to "Test $1"
            )
        val fieldDefinition = FieldMappingDefinition(associations = associations)
        val testee = FieldTargetVersionMapper(fieldDefinition)
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        issue.proprietaryTargetInstance = Mockito.mock(com.atlassian.jira.rest.client.api.domain.Issue::class.java)
        val targetClient = Mockito.mock(JiraClient::class.java)
        val value = "3.66"
        val fieldname = "target"
        `when`(targetClient.getMultiSelectValues(any(), any())).thenReturn(listOf("3.67", "3.88", "3.12"))
        // act
        testee.setValue(issue, fieldname, issue, targetClient, value)
        // assert
        val argumentCaptor = ArgumentCaptor.forClass(List::class.java)
        Mockito.verify(targetClient)
            .setValue(
                safeEq(issue),
                safeEq(issue),
                safeEq(fieldname),
                argumentCaptor.capture()
            )
        assertThat(
            "List should be correct",
            argumentCaptor.value as List<String>,
            hasItems("3.66", "3.67", "3.88", "3.12")
        )
    }
}