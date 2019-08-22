package ch.loewenfels.issuetrackingsync.settings

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.io.IOException

data class Settings(var trackingApplications: MutableList<IssueTrackingApplication> = mutableListOf()) {
    companion object {
        fun loadFromFile(fileLocation: String, objectMapper: ObjectMapper): Settings {
            val settingsFile = File(fileLocation)
            try {
                if (!settingsFile.exists()) {
                    throw IOException("Settings file " + settingsFile.absolutePath + " not found.")
                }
                return objectMapper.readValue(settingsFile, Settings::class.java)
            } catch (ex: IOException) {
                throw IllegalStateException("Failed to load settings", ex)
            }
        }
    }
}
