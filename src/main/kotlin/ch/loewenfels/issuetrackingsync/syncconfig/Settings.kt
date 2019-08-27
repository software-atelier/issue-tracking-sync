package ch.loewenfels.issuetrackingsync.syncconfig

import ch.loewenfels.issuetrackingsync.Logging
import ch.loewenfels.issuetrackingsync.logger
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.io.IOException
import java.util.*

data class Settings(
    var trackingApplications: MutableList<IssueTrackingApplication> = mutableListOf(),
    var syncFlowDefinitions: MutableList<SyncFlowDefinition> = mutableListOf()
) {
    companion object : Logging {
        fun loadFromFile(fileLocation: String, objectMapper: ObjectMapper): Settings {
            val settingsFile = File(fileLocation)
            try {
                if (!settingsFile.exists()) {
                    throw IOException("Settings file " + settingsFile.absolutePath + " not found.")
                }
                logger().info("Loading settings from {}", settingsFile.absolutePath)
                return objectMapper.readValue(settingsFile, Settings::class.java)
            } catch (ex: IOException) {
                throw IllegalStateException("Failed to load settings", ex)
            }
        }
    }

    fun getTrackingApplication(name: TrackingApplicationName): IssueTrackingApplication? =
        trackingApplications.find { Objects.equals(it.name, name) }
}
