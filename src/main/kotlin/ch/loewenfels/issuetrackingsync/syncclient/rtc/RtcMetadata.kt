package ch.loewenfels.issuetrackingsync.syncclient.rtc

import ch.loewenfels.issuetrackingsync.syncclient.IssueClientException
import com.ibm.team.workitem.client.IWorkItemClient
import com.ibm.team.workitem.common.model.*

object RtcMetadata {
    private val severities: MutableMap<String, String> = mutableMapOf()
    private val priorities: MutableMap<String, String> = mutableMapOf()

    fun getSeverityId(name: String, attribute: IAttribute, workItemClient: IWorkItemClient): Any? {
        var result = name.toLongOrNull() ?: priorities[name]
        return if (result == null) {
            loadSeverities(attribute, workItemClient)
            name.toLongOrNull() ?: severities[name]
            ?: throw IssueClientException("Unknown priority $name")
        } else {
            result
        }
    }

    fun getSeverity(internalId: String, attribute: IAttribute, workItemClient: IWorkItemClient): Any {
        var result = severities.filterValues { it == internalId }.keys.firstOrNull()
        return if (result == null) {
            loadSeverities(attribute, workItemClient)
            severities.filterValues { it == internalId }.keys.firstOrNull()
                ?: throw IssueClientException("Unknown priority $internalId")
        } else {
            result
        }
    }

    @kotlin.jvm.Synchronized
    private fun loadSeverities(attribute: IAttribute, workItemClient: IWorkItemClient) {
        val prefix = ISeverity::class.java.name
        workItemClient.resolveEnumeration(attribute, null)
            .enumerationLiterals.forEach {
            severities[it.name] = "$prefix:${it.identifier2.stringIdentifier}"
        }
    }

    fun getPriorityId(name: String, attribute: IAttribute, workItemClient: IWorkItemClient): Any? {
        var result = name.toLongOrNull() ?: priorities[name]
        return if (result == null) {
            loadPriorities(attribute, workItemClient)
            name.toLongOrNull() ?: priorities[name]
            ?: throw IssueClientException("Unknown priority $name")
        } else {
            result
        }
    }

    fun getPriorityName(internalId: String, attribute: IAttribute, workItemClient: IWorkItemClient): Any {
        var result = priorities.filterValues { it == internalId }.keys.firstOrNull()
        return if (result == null) {
            loadPriorities(attribute, workItemClient)
            priorities.filterValues { it == internalId }.keys.firstOrNull()
                ?: throw IssueClientException("Unknown priority $internalId")
        } else {
            result
        }
    }

    @kotlin.jvm.Synchronized
    private fun loadPriorities(attribute: IAttribute, workItemClient: IWorkItemClient) {
        val prefix = IPriority::class.java.name
        workItemClient.resolveEnumeration(attribute, null)
            .enumerationLiterals.forEach {
            priorities[it.name] = "$prefix:${it.identifier2.stringIdentifier}"
        }
    }
}