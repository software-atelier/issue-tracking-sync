package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.notification.NotificationObserver
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.SyncFlowDefinition
import ch.loewenfels.issuetrackingsync.syncconfig.TrackingApplicationName
import java.util.*

/**
 * This class is the main work horse of issue synchronization. The internal behavior derives from the configured
 * [SyncFlowDefinition]
 */
class SynchronizationFlow(
    syncFlowDefinition: SyncFlowDefinition,
    private val sourceClient: IssueTrackingClient,
    private val targetClient: IssueTrackingClient,
    private val notificationObserver: NotificationObserver
) {
    private val sourceApplication: TrackingApplicationName = syncFlowDefinition.source
    private val syncAction: SynchronizationAction
    private val issueFilter: IssueFilter?

    init {
        val actionClass = Class.forName(syncFlowDefinition.actionClassname) as Class<SynchronizationAction>
        syncAction = actionClass.newInstance()
        // TODO: load fieldMapper into new class which hold from/to + mapper class instance
        // pass resulting list to syncAction instance (possibly in c'tor?)
        issueFilter = syncFlowDefinition.filterClassname.let {
            val filterClass = Class.forName(it) as Class<IssueFilter>
            filterClass.newInstance()
        }
    }

    fun applies(source: TrackingApplicationName, issue: Issue): Boolean {
        return Objects.equals(sourceApplication, source) &&
                issueFilter?.test(issue) ?: true
    }

    fun execute(issue: Issue) {
        try {
            syncAction.execute(sourceClient, targetClient, issue)
            notificationObserver.notifySuccessfulSync(issue)
        } catch (ex: Exception) {
            notificationObserver.notifyException(issue, ex)
        }
    }
}