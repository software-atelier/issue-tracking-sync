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
