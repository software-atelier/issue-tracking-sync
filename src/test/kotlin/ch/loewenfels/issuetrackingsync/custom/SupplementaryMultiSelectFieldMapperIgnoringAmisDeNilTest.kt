package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.executor.fields.MultiSelectionFieldMapper
import ch.loewenfels.issuetrackingsync.safeEq
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired

internal class SupplementaryMultiSelectFieldMapperIgnoringAmisDeNilTest : AbstractSpringTest() {
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
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        `when`(targetClient.getMultiSelectValues(safeEq(issue), safeEq(jiraFieldname))).thenReturn(listOf("AKB", "IAS"))
        val value = listOf("BE", "GE")
        // act
        testee.setValue(issue, jiraFieldname, issue, targetClient, value)
        // assert
        Mockito.verify(targetClient)
            .setValue(issue, issue, jiraFieldname, arrayListOf("AKB", "IAS", "GE"))
    }


    @Test
    fun setValue_AkbAndIasAreSetBeAndTiAreValuesToWrite_AkbAndIasShouldBeSet() {
        // arrange
        val testee = buildTestee()
        val issue = TestObjects.buildIssue("MK-1")
        issue.proprietaryTargetInstance = issue
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        `when`(targetClient.getMultiSelectValues(safeEq(issue), safeEq(jiraFieldname))).thenReturn(listOf("AKB", "IAS"))
        val value = listOf("BE", "TI")
        // act
        testee.setValue(issue, jiraFieldname, issue, targetClient, value)
        // assert
        Mockito.verify(targetClient)
            .setValue(issue, issue, jiraFieldname, arrayListOf("AKB", "IAS"))
    }

    private fun buildTestee(): SupplementaryMultiSelectFieldMapperIgnoringAmisDeNil {
        val associations =
            mutableMapOf(
                "BE" to "BE",
                "TI" to "TI"
            )

        val fieldDefinition = FieldMappingDefinition(
            rtcFieldname, jiraFieldname,
            MultiSelectionFieldMapper::class.toString(), associations
        )
        return SupplementaryMultiSelectFieldMapperIgnoringAmisDeNil(fieldDefinition)
    }

}