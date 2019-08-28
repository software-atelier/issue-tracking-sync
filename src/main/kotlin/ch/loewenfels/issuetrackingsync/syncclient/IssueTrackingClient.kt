package ch.loewenfels.issuetrackingsync.syncclient;

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import com.ibm.team.workitem.common.model.IWorkItem
import java.time.LocalDateTime

interface IssueTrackingClient<T> {
    /**
     * Attempt to load an issue by unique key. Returns absent value if no issue is found, or if the client
     * is not authorized to load the issue
     */
    fun getIssue(key: String): Issue?

    /**
     * Get the issue instance as used by the implementation. For JIRA, this is a [com.atlassian.jira.rest.client.api.domain.Issue],
     * whereas for RTC it will be [IWorkItem]
     */
    fun getProprietaryIssue(issueKey: String): T?

    fun getLastUpdated(internalIssue: T): LocalDateTime

    fun getValue(internalIssue: T, fieldName: String): Any?

    fun setValue(internalIssueBuilder: Any, fieldName: String, value: Any?)

    fun createOrUpdateTargetIssue(issue: Issue, defaultsForNewIssue: DefaultsForNewIssue?)

    /**
     * Get all issues which:
     * - were created on a MASTER or PEER system since [lastPollingTimestamp]
     * - were updated since [lastPollingTimestamp] and have a reference to another tracking application
     * - were updated since [lastPollingTimestamp] and [settings.json] defines no reference fields
     *     for this tracking application
     */
    fun changedIssuesSince(lastPollingTimestamp: LocalDateTime): Collection<Issue>
}
