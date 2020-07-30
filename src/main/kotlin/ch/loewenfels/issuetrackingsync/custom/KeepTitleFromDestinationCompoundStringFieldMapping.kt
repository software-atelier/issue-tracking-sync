package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.fields.CompoundStringFieldMapper
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition

class KeepTitleFromDestinationCompoundStringFieldMapping(fieldMappingDefinition: FieldMappingDefinition) :
    CompoundStringFieldMapper(fieldMappingDefinition) {

    override fun <T> createSections(
        value: Any?,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>,
        issue: Issue
    ): MutableMap<String, String> {
        val sections = super.createSections(value, fieldname, issueTrackingClient, issue)
        if (fieldname.split(",").size == 1 && issue.proprietaryTargetInstance != null) {
            val oldVal = issueTrackingClient.getHtmlValue(issue.proprietaryTargetInstance as T, fieldname) ?: ""
            associations[""]?.let {
                val indexOf = oldVal.indexOf(it)
                if (indexOf >= 0) {
                    sections[fieldname] =
                        sections[fieldname] + oldVal.substring(oldVal.indexOf(it))
                } else {
                    sections[fieldname] = sections[fieldname] + it + "<br>"
                }
            }
        }
        return sections
    }

    override fun <T> getValue(
        proprietaryIssue: T,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>
    ): Any? {
        val value = super.getValue(proprietaryIssue, fieldname, issueTrackingClient) as String
        associations[""]?.let {
            val indexOf = value.indexOf(it)
            if (indexOf >= 0) {
                return value.substring(0, indexOf)
            }
        }
        return value
    }
}
