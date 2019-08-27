package ch.loewenfels.issuetrackingsync.controller;

import ch.loewenfels.issuetrackingsync.HTTP_PARAMNAME_ISSUEKEY
import ch.loewenfels.issuetrackingsync.HTTP_PARAMNAME_RESPONSEMESSAGE
import ch.loewenfels.issuetrackingsync.HTTP_PARAMNAME_TRACKINGSYSTEM
import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.scheduling.SyncRequestProducer
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import ch.loewenfels.issuetrackingsync.syncconfig.Settings
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OnDemandController constructor(
    private val syncRequestProducer: SyncRequestProducer,
    private val clientFactory: ClientFactory,
    private val settings: Settings
) {
    @PutMapping("/manualsync")
    fun manualSync(@RequestBody body: Map<String, String>): Map<String, String> {
        val sourceAppName = body.getValue(HTTP_PARAMNAME_TRACKINGSYSTEM);
        val trackingApp: IssueTrackingApplication? = settings.trackingApplications.find { it.name == sourceAppName }
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

    private fun retrieveIssue(key: String, trackingApplication: IssueTrackingApplication): Issue? {
        return clientFactory.getClient(trackingApplication).getIssue(key)
    }

    @GetMapping("/definedSystems")
    fun definedSystems(): List<String> {
        return settings.trackingApplications.map { app -> app.name }.toList()
    }
}
