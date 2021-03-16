package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired

@Suppress("UNCHECKED_CAST")
internal class PriorityAndSeverityFieldMapperTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory

    @Test
    fun getValue() {
        // arrange
        val testee = buildTestee()
        val issue = TestObjects.buildIssue("MK-1")
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        // act
        val result = testee.getValue(issue, "priority,severity", sourceClient)
        // assert
        assertNotNull(result)
        assertTrue(result is Pair<*, *>)
        assertEquals(
            "com.ibm.team.workitem.common.model.IPriority:priority.literal.I12",
            (result as Pair<String, String>).first
        )
        assertEquals(
            "com.ibm.team.workitem.common.model.ISeverity:severity.s2",
            result.second
        )
    }

    @Test
    fun setValue() {
        // arrange
        val testee = buildTestee()
        val issue = TestObjects.buildIssue("MK-1")
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val value = "com.ibm.team.workitem.common.model.IPriority:priority.literal.I12" to
                "com.ibm.team.workitem.common.model.ISeverity:severity.s2"

        // act
        testee.setValue(issue, "priorityId", issue, targetClient, value)
        // assert
        Mockito.verify(targetClient).setValue(issue, issue, "priorityId", "Hoch")
    }

    @Test
    fun setValue_fallback() {
        // arrange
        val testee = buildTestee()
        val issue = TestObjects.buildIssue("MK-1")
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val value = "weird_value" to "another_weird_value"

        // act
        testee.setValue(issue, "priorityId", issue, targetClient, value)
        // assert
        Mockito.verify(targetClient).setValue(issue, issue, "priorityId", "Normal")
    }

    private fun buildTestee(): PriorityAndSeverityFieldMapper {
        val iPriority = "com.ibm.team.workitem.common.model.IPriority:priority.literal.I12"
        val iSeverity = "com.ibm.team.workitem.common.model.ISeverity:severity.s2"
        val associations =
            mutableMapOf(
                iPriority + "," + iSeverity to "Hoch",
                "Hoch" to iPriority + "," + iSeverity,
                "*,*" to "Normal"
            )
        val fieldDefinition = FieldMappingDefinition(
            "priority,severity", "priorityId",
            PriorityAndSeverityFieldMapper::class.toString()
        )
        fieldDefinition.associations = associations
        return PriorityAndSeverityFieldMapper(fieldDefinition)
    }
}