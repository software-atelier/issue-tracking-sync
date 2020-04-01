package ch.loewenfels.issuetrackingsync.scheduling

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.Logging
import ch.loewenfels.issuetrackingsync.app.AppState
import ch.loewenfels.issuetrackingsync.app.SyncApplicationProperties
import ch.loewenfels.issuetrackingsync.executor.SynchronizationFlowFactory
import ch.loewenfels.issuetrackingsync.logger
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import ch.loewenfels.issuetrackingsync.syncconfig.Settings
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import javax.annotation.PostConstruct

@Component
class IssuePoller @Autowired constructor(
    private val settings: Settings,
    private val appState: AppState,
    private val syncApplicationProperties: SyncApplicationProperties,
    private val objectMapper: ObjectMapper,
    private val syncRequestProducer: SyncRequestProducer,
    private val clientFactory: ClientFactory,
    private val synchronizationFlowFactory: SynchronizationFlowFactory
) : Logging {
    private val batchSize = 200

    @PostConstruct
    fun afterPropertiesSet() {
        logger().info("Polling set for {}", syncApplicationProperties.pollingCron)
        settings.earliestSyncDate?.let {
            // we want to fail hard here as assuming a different "earliest" date might have
            // unwanted effects
            appState.lastPollingTimestamp = LocalDateTime.parse(it)
        }
    }

    @Scheduled(cron = "\${sync.pollingCron:}")
    fun checkForUpdatedIssues() {
        settings.trackingApplications.filter { it.polling }.forEach { trackingApp ->
            logger().info("Checking for issues for {}", trackingApp.name)
            val issueTrackingClient = clientFactory.getClient(trackingApp)
            pollIssuesInBatches(issueTrackingClient, trackingApp)

        }
        appState.lastPollingTimestamp = LocalDateTime.now()
        appState.persist(objectMapper)
    }

    private fun pollIssuesInBatches(
        issueTrackingClient: IssueTrackingClient<Any>,
        trackingApp: IssueTrackingApplication
    ) {
        var offset = 0
        do {
            var changedIssues: Collection<Issue> = try {
                val timestamp = appState.lastPollingTimestamp ?: LocalDateTime.now()
                issueTrackingClient.changedIssuesSince(timestamp, batchSize, offset)
            } catch (e: Exception) {
                logger().error(
                    "Could not load issues or polling. One common problem could be your authentication or authorisation." +
                            "\nException was: ${e.message}"
                )
                emptyList()
            }
            changedIssues
                .forEach { ticket ->
                    if (synchronizationFlowFactory.getSynchronizationFlow(trackingApp.name, ticket) != null) {
                        scheduleSync(ticket)
                    }
                }
            offset += batchSize

        } while (changedIssues.isNotEmpty())
    }

    private fun scheduleSync(issue: Issue) = syncRequestProducer.queue(issue)
}