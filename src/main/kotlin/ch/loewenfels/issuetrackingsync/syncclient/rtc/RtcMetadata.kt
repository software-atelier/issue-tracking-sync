package ch.loewenfels.issuetrackingsync.syncclient.rtc

import ch.loewenfels.issuetrackingsync.syncclient.IssueClientException
import com.ibm.team.workitem.client.IWorkItemClient
import com.ibm.team.workitem.common.model.*

object RtcMetadata {
    private val severities: MutableMap<String, Identifier<out ILiteral>> = mutableMapOf()
    private val priorities: MutableMap<String, Identifier<out ILiteral>> = mutableMapOf()

    fun getSeverityId(name: String, attribute: IAttribute, workItemClient: IWorkItemClient): Any? {
        return getId(severities, "severity", name, attribute, workItemClient)
    }

    fun getPriorityId(name: String, attribute: IAttribute, workItemClient: IWorkItemClient): Any? {
        return getId(priorities, "priority", name, attribute, workItemClient)
    }

    private fun getId(
        collection: MutableMap<String, Identifier<out ILiteral>>,
        property: String,
        name: String,
        attribute: IAttribute,
        workItemClient: IWorkItemClient
    ): Identifier<out ILiteral>? {
        return collection[name] ?: run {
            loadEnumeration(attribute, workItemClient, collection)
            collection[name] ?: throw IssueClientException("Unknown $property $name")
        }
    }

    fun getSeverityName(internalId: String, attribute: IAttribute, workItemClient: IWorkItemClient): Any {
        return getName(severities, "severity", internalId, attribute, workItemClient)
    }

    fun getPriorityName(internalId: String, attribute: IAttribute, workItemClient: IWorkItemClient): Any {
        return getName(priorities, "priority", internalId, attribute, workItemClient)
    }

    private fun getName(
        collection: MutableMap<String, Identifier<out ILiteral>>,
        property: String,
        internalId: String,
        attribute: IAttribute,
        workItemClient: IWorkItemClient
    ): Any {
        return getRelevantId(collection, internalId) ?: run {
            loadEnumeration(attribute, workItemClient, collection)
            getRelevantId(collection, internalId) ?: throw IssueClientException("Unknown $property $internalId")
        }
    }

    private fun getRelevantId(collection: MutableMap<String, Identifier<out ILiteral>>, internalId: String): String? {
        return collection.filterValues { it.toString() == internalId || it.stringIdentifier == internalId }
            .keys.firstOrNull()
    }

    @kotlin.jvm.Synchronized
    private fun loadEnumeration(
        attribute: IAttribute,
        workItemClient: IWorkItemClient,
        collection: MutableMap<String, Identifier<out ILiteral>>
    ) {
        workItemClient.resolveEnumeration(attribute, null)
            .enumerationLiterals.forEach {
            collection[it.name] = it.identifier2
        }
    }
}