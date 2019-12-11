package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.syncclient.rtc.RtcClient
import com.ibm.team.workitem.common.model.IWorkItem

/**
 * This class normalizes time tracking data to seconds. JIRA reports in minutes (int), RTC in milliseconds (long).
 * Furthermore, JIRA works with time spent, original estimate, and remaining estimate. RTC works
 * with time spent, duration and correction.
 *
 * - time spent (JIRA) = time spent (RTC)
 * - original estimate (JIRA) = duration (RTC)
 */
class TimeTrackingFieldMapper : FieldMapper {
    private val millisToMinutes = 1000 * 60

    override fun <T> getValue(
        proprietaryIssue: T,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>
    ): Any? {
        return when (issueTrackingClient) {
            is JiraClient -> getJiraTimeTracking(
                proprietaryIssue as com.atlassian.jira.rest.client.api.domain.Issue,
                issueTrackingClient
            )
            is RtcClient -> getRtcTimeTracking(proprietaryIssue as IWorkItem, issueTrackingClient)
            else -> TimeTracking(0, 0, 0)
        }
    }

    private fun getJiraTimeTracking(
        proprietaryIssue: com.atlassian.jira.rest.client.api.domain.Issue,
        issueTrackingClient: JiraClient
    ): TimeTracking {
        val timeSpent = issueTrackingClient.getValue(proprietaryIssue, "timeTracking.timeSpentMinutes") ?: 0
        val originalEstimate =
            issueTrackingClient.getValue(proprietaryIssue, "timeTracking.originalEstimateMinutes") ?: 0
        val remaining = issueTrackingClient.getValue(proprietaryIssue, "timeTracking.remainingEstimateMinutes") ?: 0
        return TimeTracking(timeSpent as Number, originalEstimate as Number, remaining as Number)
    }

    private fun getRtcTimeTracking(
        proprietaryIssue: IWorkItem,
        issueTrackingClient: RtcClient
    ): TimeTracking {
        val timeSpent = (issueTrackingClient.getValue(proprietaryIssue, "timeSpent") ?: 0) as Long
        val duration =
            (issueTrackingClient.getValue(proprietaryIssue, "duration") ?: 0) as Long
        val correctedEstimate =
            (issueTrackingClient.getValue(proprietaryIssue, "correctedEstimate") ?: 0) as Long
        return TimeTracking(
            timeSpent / millisToMinutes,
            duration / millisToMinutes,
            correctedEstimate / millisToMinutes
        )
    }

    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        when (issueTrackingClient) {
            is JiraClient -> setJiraTimeTracking(
                proprietaryIssueBuilder,
                issue,
                issueTrackingClient,
                value as TimeTracking
            )
            is RtcClient -> setRtcTimeTracking(
                proprietaryIssueBuilder,
                issue,
                issueTrackingClient,
                value as TimeTracking
            )
        }
    }

    private fun setJiraTimeTracking(
        proprietaryIssueBuilder: Any,
        issue: Issue,
        issueTrackingClient: JiraClient,
        timeTracking: TimeTracking
    ) {
        val jiraTimeTracking = com.atlassian.jira.rest.client.api.domain.TimeTracking(
            zeroToNull(timeTracking.originalEstimate.toInt()),
            zeroToNull(timeTracking.remaining.toInt()),
            zeroToNull(timeTracking.timeSpent.toInt())
        )
        issueTrackingClient.setValue(
            proprietaryIssueBuilder,
            issue,
            "timeTracking",
            jiraTimeTracking
        )
    }

    private fun zeroToNull(i: Int): Int? {
        return if (i <= 0) {
            null
        } else {
            i
        }
    }

    private fun setRtcTimeTracking(
        proprietaryIssueBuilder: Any,
        issue: Issue,
        issueTrackingClient: RtcClient,
        timeTracking: TimeTracking
    ) {
        issueTrackingClient.setValue(
            proprietaryIssueBuilder,
            issue,
            "timeSpent",
            timeTracking.timeSpent.toLong() * millisToMinutes
        )
        issueTrackingClient.setValue(
            proprietaryIssueBuilder,
            issue,
            "duration",
            timeTracking.originalEstimate.toLong() * millisToMinutes
        )
        // correctedEstimate is read-only
    }

    data class TimeTracking(val timeSpent: Number, val originalEstimate: Number, val remaining: Number)
}
