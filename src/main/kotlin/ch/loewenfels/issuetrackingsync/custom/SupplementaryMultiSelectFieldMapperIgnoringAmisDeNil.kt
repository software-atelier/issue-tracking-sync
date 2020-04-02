package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.fields.SupplementaryMultiSelectionFieldMapper
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition

class SupplementaryMultiSelectFieldMapperIgnoringAmisDeNil(fieldMappingDefinition: FieldMappingDefinition) :
    SupplementaryMultiSelectionFieldMapper(
        fieldMappingDefinition
    ) {
    val ignoringDoupleSyncingMap =
        mutableMapOf("AKB" to "BE", "CCNC" to "NE", "CCGC" to "GE", "IAS" to "TI")

    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        val oldValue = getValueFromTarget(issue, fieldname, issueTrackingClient)
        val newValue = mutableListOf<Any?>()
        if (value is Collection<*> && oldValue is Collection<*>) {
            newValue.addAll(value)
            ignoringDoupleSyncingMap.forEach {
                if (oldValue.contains(it.key)) {
                    newValue.remove(it.value)
                }
            }
        }
        super.setValue(proprietaryIssueBuilder, fieldname, issue, issueTrackingClient, newValue)
    }
}