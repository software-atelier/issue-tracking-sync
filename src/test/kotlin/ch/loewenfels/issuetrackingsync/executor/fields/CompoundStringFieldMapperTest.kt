package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.safeEq
import ch.loewenfels.issuetrackingsync.safeNot
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired

internal class CompoundStringFieldMapperTest : AbstractSpringTest() {
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
        val result = testee.getValue(issue, "text,text2,text3", sourceClient)
        // assert
        assertNotNull(result)
        assertEquals(
            "foobar\n" +
                    "h4. Text 2\n" +
                    "foobar\n" +
                    "h4. Text 3\n" +
                    "foobar", (result as String)
        )
    }

    @Test
    fun setValue() {
        // arrange
        val testee = buildTestee()
        val issue = TestObjects.buildIssue("MK-1")
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val value = "foobar\n\nh4. Text 2\nSome more text\nh4. Text 3\nAnd still some more"
        // act
        testee.setValue(issue, "text,text2,text3", issue, targetClient, value)
        // assert
        verify(targetClient).setValue(issue, issue, "text", "foobar")
        verify(targetClient).setValue(issue, issue, "text2", "Some more text")
        verify(targetClient).setValue(issue, issue, "text3", "And still some more")
    }

    @Test
    fun setValue_associationsFromSourceToTargetWithOneSourceFieldNameMultipleTargetFieldNames_onlyTheOneTargetFieldNameShouldGetSet() {
        // arrange
        val associations = mutableMapOf(
            "defectdescription" to "\nh4. Text2",
            "conduct" to "\nh4. Text3"
        )
        val fieldDefinitions = FieldMappingDefinition(
            "description",
            "description,defectdescription,conduct",
            CompoundStringFieldMapper::class.toString(), associations
        )
        val testee = CompoundStringFieldMapper(fieldDefinitions)
        val issue = TestObjects.buildIssue("MK-1")
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val value = "foobar\nh4. Text2\nSome more text\nh4. Text3\nAnd still some more"
        // act
        testee.setValue(issue, "description,defectdescription,conduct", issue, targetClient, value)
        // assert
        verify(targetClient).setValue(safeEq(issue), safeEq(issue), safeEq("conduct"), safeEq("And still some more"))
        verify(targetClient).setValue(
            safeEq(issue),
            safeEq(issue),
            safeEq("defectdescription"),
            safeEq("Some more text")
        )
        verify(targetClient).setValue(safeEq(issue), safeEq(issue), safeEq("description"), safeEq("foobar"))
    }

    @Test
    fun setValue_associationsFromSourceToTargetWithOneTargetFieldName_onlyTheOneTargetFieldNameShouldGetSet() {
        // arrange
        val testee = buildTestee()
        val issue = TestObjects.buildIssue("MK-1")
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val value = "foobar\nh4. Text 2\nSome more text\nh4. Text 3\nAnd still some more"
        // act
        testee.setValue(issue, "description", issue, targetClient, value)
        // assert
        verify(targetClient, never()).setValue(
            safeEq(issue),
            safeEq(issue),
            safeNot(safeEq("description")),
            anyString()
        )
    }

    private fun buildTestee(): CompoundStringFieldMapper {
        val associations =
            mutableMapOf(
                "text2" to "h4. Text 2",
                "text3" to "h4. Text 3"
            )
        val fieldDefinition = FieldMappingDefinition(
            "text,text2,text3", "description",
            CompoundStringFieldMapper::class.toString(), associations
        )
        return CompoundStringFieldMapper(fieldDefinition)
    }
}