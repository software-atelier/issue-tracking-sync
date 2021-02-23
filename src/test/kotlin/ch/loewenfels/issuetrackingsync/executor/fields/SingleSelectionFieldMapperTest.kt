package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired

class SingleSelectionFieldMapperTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory

    private val jiraFieldname = "singleSelectCustomFieldJira"
    private val rtcFieldname = "singleSelectCustomFieldRtc"

    @Test
    fun getValue() {
        // arrange
        val testee = buildTestee()
        val issue = TestObjects.buildIssue("MK-1")
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        // act
        val result = testee.getValue(issue, rtcFieldname, sourceClient)
        // assert
        assertNotNull(result)
        assertEquals("fooRtc", result)
    }

    @Test
    @Disabled
    fun setValue() {
        // arrange
        val testee = buildTestee()
        val issue = TestObjects.buildIssue("MK-1")
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val value = "fooRtc"
        // act
        testee.setValue(issue, jiraFieldname, issue, targetClient, value)
        // assert
        verify(targetClient).setValue(issue, issue, jiraFieldname, "fooJira")
    }

    @Test
    internal fun setValue_nullValue_noInteraction() {
        // arrange
        val testee = buildTestee()
        val issue = TestObjects.buildIssue("MK-1")
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        // act
        testee.setValue(issue, jiraFieldname, issue, targetClient, null)
        // assert
        verifyNoInteractions(targetClient)
    }

    private fun buildTestee(): SingleSelectionFieldMapper {
        val associations =
            mutableMapOf(
                "fooRtc" to "fooJira",
                "barRtc" to "barJira"
            )

        val fieldDefinition = FieldMappingDefinition(
            rtcFieldname, jiraFieldname,
            SingleSelectionFieldMapper::class.toString()
        )
        return SingleSelectionFieldMapper(fieldDefinition)
    }
}