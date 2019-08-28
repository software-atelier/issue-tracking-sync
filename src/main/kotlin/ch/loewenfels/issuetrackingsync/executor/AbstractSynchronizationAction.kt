package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.SynchronizationAbortedException
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import java.time.temporal.ChronoUnit
import kotlin.math.abs

abstract class AbstractSynchronizationAction {
    protected fun buildTargetIssueKey(
        sourceClient: IssueTrackingClient<Any>,
        issue: Issue,
        keyfieldMapping: KeyFieldMapping
    ) {
        val sourceIssue =
            sourceClient.getProprietaryIssue(issue.key) ?: throw IllegalArgumentException("No source issue found")
        val gap = issue.lastUpdated.until(sourceClient.getLastUpdated(sourceIssue), ChronoUnit.SECONDS)
        if (abs(gap) > 5) {
            throw SynchronizationAbortedException("Issues has been updated since synchronization request")
        }
        issue.proprietarySourceInstance = sourceIssue
        keyfieldMapping.loadSourceValue(issue, sourceClient)
        issue.keyFieldMapping = keyfieldMapping
    }

    protected fun buildTargetIssueValues(
        sourceClient: IssueTrackingClient<Any>,
        issue: Issue,
        fieldMappings: List<FieldMapping>
    ) {
        fieldMappings.forEach {
            it.loadSourceValue(issue, sourceClient)
            issue.fieldMappings.add(it)
        }
    }

    protected fun createOrUpdateTargetIssue(
        targetClient: IssueTrackingClient<Any>,
        issue: Issue,
        defaultsForNewIssue: DefaultsForNewIssue?
    ) {
        targetClient.createOrUpdateTargetIssue(issue, defaultsForNewIssue)
    }
}