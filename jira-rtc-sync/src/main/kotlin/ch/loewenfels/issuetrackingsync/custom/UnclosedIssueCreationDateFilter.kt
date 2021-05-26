package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.syncclient.rtc.RtcClient
import ch.loewenfels.issuetrackingsync.syncconfig.SyncFlowDefinition
import com.ibm.team.workitem.common.model.IWorkItem
import org.joda.time.DateTime
import java.time.LocalDateTime

abstract class UnclosedIssueCreationDateFilter : UnclosedFilter() {
    var parameters: Map<String, String> = emptyMap()

    override fun defineParameters(parameters: Map<String, String>) {
        this.parameters = parameters
    }


    override fun testUnclosedIssueInJira(
        client: JiraClient,
        issue: Issue,
        syncFlowDefinition: SyncFlowDefinition
    ): Boolean {
        val internalIssue = client.getProprietaryIssue(issue.key) as com.atlassian.jira.rest.client.api.domain.Issue
        val creationDate = (client.getValue(internalIssue, "creationDate") as DateTime?)
        val creationLocalDateTime = creationDate?.let {
            LocalDateTime
                .of(it.year, it.monthOfYear, it.dayOfMonth, it.hourOfDay, it.minuteOfHour)
        }
        return super.testUnclosedIssueInJira(client, issue, syncFlowDefinition) && isCreationDateCorrect(
            creationLocalDateTime
        )
    }

    override fun testUnclosedIssueInRtc(
        client: RtcClient,
        issue: Issue,
        syncFlowDefinition: SyncFlowDefinition
    ): Boolean {
        val internalIssue = client.getProprietaryIssue(issue.key) as IWorkItem
        val creationDate = (client.getValue(internalIssue, "creationDate") as java.sql.Timestamp?)?.toLocalDateTime()
        return super.testUnclosedIssueInRtc(client, issue, syncFlowDefinition) && isCreationDateCorrect(creationDate)
    }

    private fun isCreationDateCorrect(creationDate: LocalDateTime?): Boolean =
        creationDate?.let { checkCreationAfterDate(it) ?: createdBeforeDate()?.isAfter(it) } ?: true


    private fun checkCreationAfterDate(creationDate: LocalDateTime) =
        createdAfterDate()?.let { creationDate.isEqual(it) || creationDate.isAfter(it) }


    fun createdAfterDate(): LocalDateTime? =
        parameters["createdAfter"]?.let(LocalDateTime::parse)


    fun createdBeforeDate(): LocalDateTime? =
        parameters["createdBefore"]?.let(LocalDateTime::parse)

}