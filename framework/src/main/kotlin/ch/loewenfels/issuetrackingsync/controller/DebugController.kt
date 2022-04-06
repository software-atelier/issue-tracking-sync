package ch.loewenfels.issuetrackingsync.controller

import ch.loewenfels.issuetrackingsync.app.SyncApplicationProperties
import ch.loewenfels.issuetrackingsync.scheduling.IssuePoller
import ch.loewenfels.issuetrackingsync.syncconfig.Settings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import kotlin.concurrent.thread

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
    triggerPollingManually()
  }

  @GetMapping("/triggerPolling")
  fun triggerPollingManually(): String {
    thread(start = true) {
      issuePoller.checkForUpdatedIssues()
    }
    return "Trigger successfully started. Please look at the protocol for further status information"
  }

  @GetMapping("/config")
  fun getSettingsFile(): RedirectView {
    return RedirectView(settings.configLink) // TC-264
  }

  @GetMapping("/log")
  fun getLogFile(): RedirectView {
    return RedirectView(settings.logsLink) // TC-264
  }
}