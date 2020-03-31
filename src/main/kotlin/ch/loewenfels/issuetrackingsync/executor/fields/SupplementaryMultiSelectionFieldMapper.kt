package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition

class SupplementaryMultiSelectionFieldMapper(fieldMappingDefinition: FieldMappingDefinition) :
    MultiSelectionFieldMapper(
        fieldMappingDefinition
    ) {
    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        val newValue = mutableSetOf<Any>()
        val oldValue =
            super.getValue(
                issue.proprietaryTargetInstance as T,
                fieldname,
                issueTrackingClient,
                mapOf("*" to "*")
            )
        addToSet(newValue, oldValue)
        addToSet(newValue, value)
        super.setValue(
            proprietaryIssueBuilder,
            fieldname,
            issue,
            issueTrackingClient,
            newValue.toList()
        )
    }

    private fun addToSet(setNewValues: MutableSet<Any>, toMergeIntoNewValues: Any?) {
        toMergeIntoNewValues?.let {
            if (toMergeIntoNewValues is Collection<*>) {
                setNewValues.addAll(toMergeIntoNewValues.filterNotNull())
            } else {
                setNewValues.add(toMergeIntoNewValues)
            }
        }
    }
}