package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.fields.SupplementaryMultiSelectionFieldMapper
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition

class SupplementaryMultiSelectFieldMapperIgnoringAmisDeNil(fieldMappingDefinition: FieldMappingDefinition) :
    SupplementaryMultiSelectionFieldMapper(
        fieldMappingDefinition
    ) {
    val defaultValueOnlyIfEmpty = "Jira2RTC"
    val ignoringDoupleSyncingMap =
        mutableMapOf("AKB" to "BE", "CCNC" to "NE", "CCGC" to "GE", "IAS" to "TI")

    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        val newValue = mutableSetOf<Any>()
        val oldValue = getValueFromTarget(issue, fieldname, issueTrackingClient)
        addToSet(newValue, oldValue)
        addToSet(newValue, value)
        addToSet(newValue, defaultValueOnlyIfEmpty)
        ignoringDoupleSyncingMap.forEach {
            if (oldValue.contains(it.key)) {
                newValue.remove(it.value)
            }
        }

        if (newValue.size > 1) {
            if (newValue.contains(defaultValueOnlyIfEmpty)) newValue.remove(defaultValueOnlyIfEmpty)
        }

        issueTrackingClient.setValue(proprietaryIssueBuilder, issue, fieldname, newValue.toList())
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