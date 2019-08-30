package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient

open class FieldMapping(
    private val sourceName: String,
    protected val targetName: String,
    private val mapper: FieldMapper
) {
    protected var sourceValue: Any? = null
    @Suppress("UNCHECKED_CAST")
    fun <T> loadSourceValue(issue: Issue, issueTrackingClient: IssueTrackingClient<in T>) {
        if (issue.proprietarySourceInstance == null) {
            throw IllegalStateException("Internal source issue needs to be loaded first")
        }
        sourceValue = mapper.getValue(issue.proprietarySourceInstance as T, sourceName, issueTrackingClient)
    }

    fun <T> setTargetValue(issueBuilder: Any, targetClient: IssueTrackingClient<in T>) {
        mapper.setValue(issueBuilder, targetName, targetClient, sourceValue)
    }
}