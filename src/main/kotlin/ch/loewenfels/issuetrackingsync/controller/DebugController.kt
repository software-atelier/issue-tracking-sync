package ch.loewenfels.issuetrackingsync.controller

import ch.loewenfels.issuetrackingsync.scheduling.IssuePoller
import ch.loewenfels.issuetrackingsync.syncconfig.Settings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DebugController(
    private val settings: Settings
) {
    @Autowired
    lateinit var issuePoller: IssuePoller

    @GetMapping("/manualTimetrackingSync")
    fun startManualTimetrackingSync() {
        issuePoller.checkForUpdatedIssues();
    }

    @GetMapping("/config")
    fun getSettingsFile(): Settings {
        val modifiedSettings = settings
        modifiedSettings.trackingApplications.forEach { it.password = "*********" }
        return modifiedSettings
    }
}