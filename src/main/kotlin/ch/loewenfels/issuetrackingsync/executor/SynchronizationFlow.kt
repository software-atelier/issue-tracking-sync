package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.Logging
import ch.loewenfels.issuetrackingsync.logger
import ch.loewenfels.issuetrackingsync.notification.NotificationObserver
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import ch.loewenfels.issuetrackingsync.syncconfig.SyncFlowDefinition
import ch.loewenfels.issuetrackingsync.syncconfig.TrackingApplicationName
import java.util.*

/**
 * This class is the main work horse of issue synchronization. The internal behavior derives from the configured
 * [SyncFlowDefinition]
 */
class SynchronizationFlow(
    syncFlowDefinition: SyncFlowDefinition,
    private val sourceClient: IssueTrackingClient<Any>,
    private val targetClient: IssueTrackingClient<Any>,
    private val notificationObserver: NotificationObserver
) : Logging {
    private val sourceApplication: TrackingApplicationName = syncFlowDefinition.source
    private val syncAction: SynchronizationAction
    private val issueFilter: IssueFilter?
    private val keyFieldMapping: KeyFieldMapping
    private val fieldMappings: List<FieldMapping>
    private val defaultsForNewIssue: DefaultsForNewIssue?

    init {
        val actionClass = Class.forName(syncFlowDefinition.actionClassname) as Class<SynchronizationAction>
        syncAction = actionClass.newInstance()
        issueFilter = syncFlowDefinition.filterClassname?.let {
            val filterClass = Class.forName(it) as Class<IssueFilter>
            filterClass.newInstance()
        }
        keyFieldMapping = FieldMappingFactory.getKeyMapping(syncFlowDefinition.keyFieldMappingDefinition)
        fieldMappings = syncFlowDefinition.fieldMappingDefinitions.map { FieldMappingFactory.getMapping(it) }.toList()
        defaultsForNewIssue = syncFlowDefinition.defaultsForNewIssue
    }

    fun applies(source: TrackingApplicationName, issue: Issue): Boolean {
        return Objects.equals(sourceApplication, source) &&
                issueFilter?.test(issue) ?: true
    }

    fun execute(issue: Issue) {
        try {
            syncAction.execute(sourceClient, targetClient, issue, keyFieldMapping, fieldMappings, defaultsForNewIssue)
            notificationObserver.notifySuccessfulSync(issue)
        } catch (ex: Exception) {
            logger().debug(ex.message, ex)
            notificationObserver.notifyException(issue, ex)
        }
    }
}