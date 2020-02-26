package ch.loewenfels.issuetrackingsync.controller

import ch.loewenfels.issuetrackingsync.app.SyncApplicationProperties
import ch.loewenfels.issuetrackingsync.scheduling.IssuePoller
import ch.loewenfels.issuetrackingsync.syncconfig.Settings
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileInputStream
import javax.servlet.http.HttpServletResponse

@RestController
class DebugController(
    private val settings: Settings
) {
    @Autowired
    lateinit var issuePoller: IssuePoller
    @Autowired
    lateinit var syncApplicationProperties: SyncApplicationProperties

    @GetMapping("/manualTimetrackingSync")
    fun startManualTimetrackingSync() {
        issuePoller.checkForUpdatedIssues()
    }

    @GetMapping("/config")
    fun getSettingsFile(): Settings {
        val modifiedSettings = settings
        modifiedSettings.trackingApplications.forEach { it.password = "*********" }
        return modifiedSettings
    }

    @GetMapping("/log{date}")
    fun getLogFile(response: HttpServletResponse, @PathVariable("date") date: String) {
        val file: File = getFile(date)
        response.contentType = "text/plain"
        response.setHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
        FileInputStream(file).use { IOUtils.copy(it, response.outputStream) }
    }

    private fun getFile(date: String): File {
        val dateSuffix = "0.log"
        return if (date.isBlank()) {
            File(syncApplicationProperties.logfile)
        } else {
            File("${syncApplicationProperties.logfile}.$date.$dateSuffix")
        }
    }
}