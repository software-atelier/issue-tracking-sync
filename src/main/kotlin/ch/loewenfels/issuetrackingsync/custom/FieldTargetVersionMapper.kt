package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.fields.FieldValueRegexTransformationMapper
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import ch.loewenfels.issuetrackingsync.syncclient.rtc.RtcClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import com.ibm.team.workitem.common.model.IWorkItem

class FieldTargetVersionMapper(fieldMappingDefinition: FieldMappingDefinition) :
    FieldValueRegexTransformationMapper(fieldMappingDefinition) {

    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        when (issueTrackingClient) {
            is RtcClient -> mergeLogicToRtc(proprietaryIssueBuilder, fieldname, issue, issueTrackingClient, value)
            is JiraClient -> mergeLogicToJira(proprietaryIssueBuilder, fieldname, issue, issueTrackingClient, value)
        }
    }

    private fun mergeLogicToRtc(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: RtcClient,
        value: Any?
    ) {
        if (value is List<*>) {
            val value1: String = super.getValue(
                issue.proprietaryTargetInstance as IWorkItem,
                fieldname,
                issueTrackingClient,
                mapOf("(.*)" to "$1")
            ) as String
            val get = "^I\\d{4}\\.\\d+ - (\\d\\.\\d{2,3}.*)".toRegex().find(value1)?.groupValues?.get(1) ?: ""
            if (value.contains(get).not()) {
                mergeToRtc(value, issue, issueTrackingClient, proprietaryIssueBuilder, fieldname)
            }
        }
    }

    private fun mergeToRtc(
        value: List<*>,
        issue: Issue,
        issueTrackingClient: RtcClient,
        proprietaryIssueBuilder: Any,
        fieldname: String
    ) {
        val first = mutableListOf<String>()
        val second = mutableListOf<String>()
        for (string in value) {
            if (string is String) {
                val regexMinorVersion = "^(\\d\\.\\d{2,3})(?!\\.)".toRegex()
                val regexBugfixVerion = "^(\\d\\.\\d{2,3}\\.\\d*)".toRegex()
                first.addAll(regexMinorVersion.findAll(string).toList().map { it.groupValues.get(1) })
                second.addAll(regexBugfixVerion.findAll(string).toList().map { it.groupValues.get(1) })
            }
        }
        first.sort()
        second.sort()
        val potentialValueToWrite = first.firstOrNull() ?: second.firstOrNull()
        checkNotNull(potentialValueToWrite) {
            "The state of the issue ${issue.key} is not valid. No legit sync value found for TargetVersion value was: $value"
        }
        val valueToWrite =
            issueTrackingClient.getAllIIteration().map { it.name }.firstOrNull { it.endsWith(potentialValueToWrite) }
        checkNotNull(valueToWrite) { IllegalStateException("The version is not yet defined for RTC. Version: $potentialValueToWrite") }
        super.setValue(proprietaryIssueBuilder, fieldname, issue, issueTrackingClient, valueToWrite)
    }

    fun mergeLogicToJira(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: JiraClient,
        value: Any?
    ) {
        if (value is String) {
            val value1 = issueTrackingClient.getMultiSelectValues(
                issue.proprietaryTargetInstance as com.atlassian.jira.rest.client.api.domain.Issue,
                fieldname
            )
            if (value1.contains(value).not()) {
                val regexMinorVersion = "^\\d\\.\\d{2,3}(?!\\.)".toRegex()
                val regexBugfixVersion = "\\d\\.\\d{2,3}\\.\\d*(?!\\.)".toRegex()
                val valueToWrite = value1.toMutableList()

                check(regexMinorVersion.containsMatchIn(value) || regexBugfixVersion.containsMatchIn(value)) {
                    throw IllegalStateException(
                        "The version of the issue ${issue.key} is not valid. No legit sync " +
                                "value found for TargetVersion value was: $value"
                    )
                }
                valueToWrite.add(value)
                super.setValue(
                    proprietaryIssueBuilder,
                    fieldname,
                    issue,
                    issueTrackingClient,
                    valueToWrite
                )
            }

        }
    }
}