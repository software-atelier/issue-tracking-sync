package ch.loewenfels.issuetrackingsync.testcontext

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.*
import ch.loewenfels.issuetrackingsync.notification.NotificationObserver
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.*
import org.mockito.Mockito.spy
import java.time.LocalDateTime

object TestObjects {
    fun buildSyncFlowDefinition(source: TrackingApplicationName, target: TrackingApplicationName): SyncFlowDefinition {
        val syncFlowDefinition = SyncFlowDefinition()
        syncFlowDefinition.source = source
        syncFlowDefinition.target = target
        syncFlowDefinition.actionClassname = SimpleSynchronizationAction::class.qualifiedName ?: ""
        syncFlowDefinition.defaultsForNewIssue = buildDefaultsForNewIssue()
        syncFlowDefinition.keyFieldMappingDefinition = buildKeyFieldMappingDefinition()
        syncFlowDefinition.fieldMappingDefinitions = buildFieldMappingDefinitionList()

        return syncFlowDefinition
    }

    fun buildKeyFieldMappingDefinition(): KeyFieldMappingDefinition =
        KeyFieldMappingDefinition("key", "custom_field_10244", "ch.foobar.team.workitem.attribute.external_refid")

    fun buildFieldMappingDefinitionList(): MutableList<FieldMappingDefinition> =
        mutableListOf(buildFieldMappingDefinition())

    fun buildFieldMappingDefinition(): FieldMappingDefinition =
        FieldMappingDefinition("title", "summary")

    fun buildKeyFieldMapping(): KeyFieldMapping =
        KeyFieldMapping("key", "id", "ch.foobar.team.workitem.attribute.external_refid", DirectFieldMapper())

    fun buildFieldMappingList(): MutableList<FieldMapping> = mutableListOf(buildFieldMapping())

    fun buildFieldMapping(): FieldMapping =
        FieldMapping("title", "summary", DirectFieldMapper())

    fun buildDefaultsForNewIssue(): DefaultsForNewIssue = DefaultsForNewIssue("task", "BUG")

    fun buildIssueTrackingApplication(simpleClassName: String): IssueTrackingApplication {
        val issueTrackingApplication = IssueTrackingApplication()
        issueTrackingApplication.className = simpleClassName
        issueTrackingApplication.name = simpleClassName.toUpperCase()
        return issueTrackingApplication
    }

    fun buildIssueTrackingClient(
        issueTrackingApplication: IssueTrackingApplication,
        clientFactory: ClientFactory
    ): IssueTrackingClient<Any> =
        spy(clientFactory.getClient(issueTrackingApplication))

    fun buildNotificationObserver(): NotificationObserver {
        val observer = NotificationObserver()
        observer.addChannel(SynchronizationFlowTest)
        return observer
    }

    fun buildIssue(key: String = "MK-1") =
        Issue(key, "", LocalDateTime.now())
}

class AlwaysTrueIssueFilter : IssueFilter {
    override fun test(issue: Issue): Boolean = true
}

class AlwaysFalseIssueFilter : IssueFilter {
    override fun test(issue: Issue): Boolean = false
}