package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired

internal class SupplementaryMultiSelectionFieldMapperTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory

    private val jiraFieldname = "multiSelectCustomFieldJira"
    private val rtcFieldname = "multiSelectCustomFieldRtc"

    @Test
    fun setValue() {
        // arrange
        val testee = buildTestee()
        val issue = TestObjects.buildIssue("MK-1")
        issue.proprietaryTargetInstance = issue
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        val value = listOf("congress")
        // act
        testee.setValue(issue, jiraFieldname, issue, targetClient, value)
        // assert
        Mockito.verify(targetClient)
            .setValue(issue, issue, jiraFieldname, arrayListOf("fooJira", "barJira", "congress"))
    }

    private fun buildTestee(): SupplementaryMultiSelectionFieldMapper {
        val associations =
            mutableMapOf(
                "fooRtc" to "fooJira",
                "barRtc" to "barJira"
            )

        val fieldDefinition = FieldMappingDefinition(
            rtcFieldname, jiraFieldname,
            MultiSelectionFieldMapper::class.toString(), associations
        )
        return SupplementaryMultiSelectionFieldMapper(fieldDefinition)
    }

}