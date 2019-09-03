package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.Logging
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition

/**
 * This class can take several text-based properties and map them to a single field, creating
 * sections using [associations]. For each property field, this class epxects an association
 * with the fieldName as key and a section header as value.
 *
 * This class can also take a single text-based property and split the content into multiple
 * fields, using the same [associations] mechanism. Note that the order of properties matters;
 * the 'catch-all' property must be listed last
 */
class CompoundStringFieldMapper(fieldMappingDefinition: FieldMappingDefinition) : HtmlToWikiFieldMapper(), Logging {
    private val associations: Map<String, String> = fieldMappingDefinition.associations

    override fun <T> getValue(
        proprietaryIssue: T,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>
    ): Any? {
        val stringBuilder = StringBuilder()
        fieldname.split(",").forEach { propertyName ->
            val contentPart = super.getValue(proprietaryIssue, propertyName, issueTrackingClient)?.toString() ?: ""
            if (contentPart.isNotEmpty()) {
                associations[propertyName]?.let {
                    stringBuilder.append(it).append("\n")
                }
                stringBuilder.append(contentPart).append("\n")
            }
        }
        return stringBuilder.toString().trim()
    }

    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        val sections: MutableMap<String, String> = mutableMapOf()
        var remainingStringValue = value?.toString() ?: ""
        var propertyWithLastHeader = getPropertyWithLastHeader(remainingStringValue)
        while (propertyWithLastHeader != null) {
            val headerStart = remainingStringValue.indexOf(associations[propertyWithLastHeader]!!)
            sections[propertyWithLastHeader] =
                remainingStringValue.substring(headerStart + associations[propertyWithLastHeader]!!.length)
            remainingStringValue = remainingStringValue.substring(0, headerStart)
            propertyWithLastHeader = getPropertyWithLastHeader(remainingStringValue)
        }
        if (remainingStringValue.isNotEmpty()) {
            val catchAllProperty = fieldname.split(",").first { !sections.keys.contains(it) }
            sections[catchAllProperty] = remainingStringValue
        }
        sections.forEach { (propertyName, content) ->
            super.setValue(proprietaryIssueBuilder, propertyName, issue, issueTrackingClient, content.trim())
        }
    }

    private fun getPropertyWithLastHeader(content: String): String? {
        var indexOfHeader = 0
        var propertyWithLastHeader: String? = null
        associations.forEach { (propertyName, header) ->
            val headerStart = content.indexOf(header)
            if (headerStart > 0 && headerStart > indexOfHeader) {
                indexOfHeader = headerStart
                propertyWithLastHeader = propertyName
            }
        }
        return propertyWithLastHeader
    }
}