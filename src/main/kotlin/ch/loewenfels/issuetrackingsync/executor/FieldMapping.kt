package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient

open class FieldMapping(
    private val sourceName: String,
    private val targetName: String,
    private val mapper: FieldMapper
) {
    protected var sourceValue: Any? = null

    fun <T> loadSourceValue(issue: Issue, issueTrackingClient: IssueTrackingClient<in T>) {
        if (issue.proprietarySourceInstance == null) {
            throw IllegalStateException("Internal source issue needs to be loaded first")
        }
        sourceValue = mapper.getValue(issue.proprietarySourceInstance as T, sourceName, issueTrackingClient)
    }

    fun <T> setTargetValue(issue: Issue, targetClient: IssueTrackingClient<in T>) {
        mapper.setValue(issue, targetName, targetClient, sourceValue)
    }
}