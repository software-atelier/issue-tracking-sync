package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition

open class FieldValueRegexTransformationMapper(fieldMappingDefinition: FieldMappingDefinition) : FieldMapper {
    val associations = fieldMappingDefinition.associations

    override fun <T> getValue(
        proprietaryIssue: T,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>
    ): Any? {
        return getValue(proprietaryIssue, fieldname, issueTrackingClient, associations)
    }


    protected fun <T> getValue(
        proprietaryIssue: T,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>,
        association: Map<String, String>
    ): Any? {
        val value = issueTrackingClient.getValue(proprietaryIssue, fieldname)
        if (value is String) {
            return transFromString(value, association)
        } else if (value is List<*>) {
            val list = issueTrackingClient.getMultiSelectValues(proprietaryIssue, fieldname)
            return list.map { transFromString(it, association) }
        }
        return value
    }

    private fun transFromString(value: String, association: Map<String, String>): String? {
        return association.map {
            it.key.toRegex().find(value)?.value?.replace(it.key.toRegex(), it.value)
        }.filterNotNull().firstOrNull(String::isNotEmpty)
    }

    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        issueTrackingClient.setValue(proprietaryIssueBuilder, issue, fieldname, value)
    }

}