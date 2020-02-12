package ch.loewenfels.issuetrackingsync.controller

import ch.loewenfels.issuetrackingsync.HTTP_PARAMNAME_ISSUEKEY
import ch.loewenfels.issuetrackingsync.HTTP_PARAMNAME_RESPONSEMESSAGE
import ch.loewenfels.issuetrackingsync.HTTP_PARAMNAME_TRACKINGSYSTEM
import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.app.AppState
import ch.loewenfels.issuetrackingsync.app.SyncApplicationProperties
import ch.loewenfels.issuetrackingsync.scheduling.SyncRequestProducer
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import ch.loewenfels.issuetrackingsync.syncconfig.Settings
import org.springframework.scheduling.support.CronSequenceGenerator
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date

@RestController
class InteractivceSyncController(
    private val syncRequestProducer: SyncRequestProducer,
    private val clientFactory: ClientFactory,
    private val settings: Settings,
    private val syncApplicationProperties: SyncApplicationProperties,
    private val appState: AppState
) {

    @GetMapping("/systeminfo")
    fun getSystemInfo(): Map<String, Any> {
        return mapOf(
            "title" to syncApplicationProperties.title
        )
    }

    @GetMapping("/info")
    fun getSettings(): Map<String, String> {
        val dateFormat = "dd. MMM. yyyy HH:mm:ss"
        val cronTrigger = CronSequenceGenerator(syncApplicationProperties.pollingCron)
        val nextPollingDate = cronTrigger.next(Date())
        return mapOf(
            "Next run" to SimpleDateFormat(dateFormat).format(nextPollingDate),
            "Process issues updated after" to DateTimeFormatter.ofPattern(dateFormat).format(appState.lastPollingTimestamp)
        )
    }

    @PutMapping("/manualsync")
    fun manualSync(@RequestBody body: Map<String, String>): Map<String, String> {
        val sourceAppName = body.getValue(HTTP_PARAMNAME_TRACKINGSYSTEM)
        val trackingApp: IssueTrackingApplication? =
            settings.trackingApplications.find { it.name.equals(sourceAppName, ignoreCase = true) }
        val resultMessage = trackingApp?.let { loadAndQueueIssue(body.getValue(HTTP_PARAMNAME_ISSUEKEY), it) }
            ?: "Unknown source app $sourceAppName. Are your settings correct?"
        return mapOf(HTTP_PARAMNAME_RESPONSEMESSAGE to resultMessage)
    }

    private fun loadAndQueueIssue(key: String, trackingApplication: IssueTrackingApplication): String {
        return retrieveIssue(key, trackingApplication)?.let {
            syncRequestProducer.queue(it)
            "Issue $key has been queued for sync"
        } ?: "Failed to locate issue with key $key in ${trackingApplication.name}"
    }

    private fun retrieveIssue(key: String, trackingApplication: IssueTrackingApplication): Issue? =
        clientFactory.getClient(trackingApplication).getIssue(key)

    @GetMapping("/definedSystems")
    fun definedSystems(): List<String> =
        settings.trackingApplications.map { app -> app.name }.toList()
}
