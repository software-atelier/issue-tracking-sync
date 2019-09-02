package ch.loewenfels.issuetrackingsync.syncconfig

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class SettingsTest : AbstractSpringTest() {
    @Autowired
    lateinit var objectMapper: ObjectMapper;

    @Test
    fun loadFromFile_validParameters_fileProperlyLoaded() {
        // arrange
        val classpathResource = javaClass.getResource("/settings.json")
        assertNotNull(classpathResource, "Failed to locate settings.json")
        // act
        val result = Settings.loadFromFile(classpathResource.getPath(), objectMapper)
        // assert
        assertEquals(2, result.trackingApplications.size, "Count of configured tracking applications")
        assertEquals(1, result.actionDefinitions.size, "Count of configured actions")
        val actionDefinition = result.actionDefinitions[0]
        assertEquals(2, actionDefinition.fieldMappingDefinitions.size, "Count of field mappings in first action")
        assertEquals(
            "summary",
            actionDefinition.fieldMappingDefinitions[0].sourceName,
            "Source property of first field mapping"
        )
        assertEquals(1, result.syncFlowDefinitions.size, "Count of configured flows")
        val syncFlow = result.syncFlowDefinitions[0]
        assertEquals("id", syncFlow.keyFieldMappingDefinition.sourceName, "source property name for key field mapping")
        assertEquals(
            "RTC ID",
            syncFlow.keyFieldMappingDefinition.targetName,
            "target property name for key field mapping"
        )
        assertEquals(
            "key",
            syncFlow.writeBackFieldMappingDefinition?.sourceName,
            "source property name for write-back mapping"
        )
        assertEquals(
            "ch.loewenfels.team.workitem.attribute.external_refid",
            syncFlow.writeBackFieldMappingDefinition?.targetName,
            "target property name for write-back mapping"
        )
        assertNotNull(syncFlow.defaultsForNewIssue)
        assertEquals("TST", syncFlow.defaultsForNewIssue?.project, "Default project for new issues")
        assertEquals(1, syncFlow.actions.size, "Count of actions in sync flow")
    }

    @Test
    fun serialize_validSettingsDataStructure_fileProperlyLoaded() {
        // arrange
        val jira = IssueTrackingApplication();
        jira.name = "JIRA";
        jira.username = "foobar";
        val settings = Settings();
        settings.trackingApplications.add(jira);
        // act
        val serialized = objectMapper.writeValueAsString(settings);
        // assert
        assertThat(serialized, containsString("foobar"));
    }
}
