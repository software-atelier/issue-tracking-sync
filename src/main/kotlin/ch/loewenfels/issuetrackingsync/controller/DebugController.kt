package ch.loewenfels.issuetrackingsync.controller

import ch.loewenfels.issuetrackingsync.syncconfig.Settings
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DebugController(
    private val settings: Settings
) {
    @GetMapping("/config")
    fun getSettingsFile(): Settings {
        return settings
    }
}