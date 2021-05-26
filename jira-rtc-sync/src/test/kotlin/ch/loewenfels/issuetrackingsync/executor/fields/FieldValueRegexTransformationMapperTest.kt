package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects.buildIssue
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects.buildIssueTrackingApplication
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects.buildIssueTrackingClient
import com.atlassian.jira.rest.client.api.domain.Issue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired

internal class FieldValueRegexTransformationMapperTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory

    @Test
    fun regexIncludeAllLeadingAndTrailingChars_getValue_newValueGenerated() {
        // arrange
        val fieldMappingDefinition = FieldMappingDefinition(
            associations = mutableMapOf(
                "I{1}\\d{4}\\.{1}\\d{1} - (\\d{1}\\.\\d{2})" to "Test $1"
            )
        )
        fieldMappingDefinition.associations = mutableMapOf(Pair(".*(\\d{1}).*", "$1"))
        val testee = FieldValueRegexTransformationMapper(fieldMappingDefinition)
        val issue = mock(Issue::class.java)
        val sourceClient = mock(JiraClient::class.java)
        `when`(sourceClient.getValue(issue, "description"))
            .thenReturn("This is a random text with only 1 number in it")
        // act
        val result = testee.getValue(issue, "description", sourceClient)
        // assert
        assertEquals("1", result)
    }

    @Test
    fun notIncludedTrailingAndLeadingChars_getValue_newValueGenerated() {
        // arrange
        val fieldMappingDefinition = FieldMappingDefinition(
            associations = mutableMapOf(
                "I{1}\\d{4}\\.{1}\\d{1} - (\\d{1}\\.\\d{2})" to "Test $1"
            )
        )
        fieldMappingDefinition.associations = mutableMapOf(Pair("(\\d{1})", "$1"))
        val testee = FieldValueRegexTransformationMapper(fieldMappingDefinition)
        val issue = mock(Issue::class.java)
        val sourceClient = mock(JiraClient::class.java)
        `when`(sourceClient.getValue(issue, "description"))
            .thenReturn("This is a random text with only 1 number in it")
        // act
        val result = testee.getValue(issue, "description", sourceClient)
        // assert
        assertEquals("1", result)
    }

    @Test
    fun readingFromRtcClientGeplantFuer_getValue_newStringGeneratedBasedOnAssociations() {
        // arrange
        val testee = buildTestee()
        val issue = buildIssue("MK-1")
        val sourceClient = buildIssueTrackingClient(buildIssueTrackingApplication("RtcClient"), clientFactory)
        // act
        val result = testee.getValue(issue, "geplantFuer", sourceClient)
        // assert
        assertNotNull(result)
        assertEquals("Test 3.66", result)
    }

    private fun buildTestee(): FieldValueRegexTransformationMapper {
        val fieldDefinition = FieldMappingDefinition(
            associations = mutableMapOf(
                "I{1}\\d{4}\\.{1}\\d{1} - (\\d{1}\\.\\d{2})" to "Test $1"
            )
        )
        return FieldValueRegexTransformationMapper(fieldDefinition)
    }
}