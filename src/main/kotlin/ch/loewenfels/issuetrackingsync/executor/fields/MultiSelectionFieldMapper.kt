package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition

class MultiSelectionFieldMapper(fieldMappingDefinition: FieldMappingDefinition) : FieldMapper {
    private val associations: Map<String, String> = fieldMappingDefinition.associations

    override fun <T> getValue(
        proprietaryIssue: T,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>
    ): Any? {
        val values = issueTrackingClient.getMultiSelectEnumeration(proprietaryIssue, fieldname)
        return values.filter { associations.containsKey(it) }
    }

    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        val result = (value as ArrayList<*>).filterIsInstance<String>()
            .filter { associations.containsKey(it) }//
            .map { associations.getValue(it) }
        issueTrackingClient.setValue(proprietaryIssueBuilder, issue, fieldname, result)
    }
}