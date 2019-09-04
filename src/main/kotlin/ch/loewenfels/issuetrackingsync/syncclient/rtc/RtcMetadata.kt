package ch.loewenfels.issuetrackingsync.syncclient.rtc

import ch.loewenfels.issuetrackingsync.syncclient.IssueClientException
import com.ibm.team.workitem.client.IWorkItemClient
import com.ibm.team.workitem.common.model.*

object RtcMetadata {
    private val severities: MutableMap<String, Identifier<out ILiteral>> = mutableMapOf()
    private val priorities: MutableMap<String, Identifier<out ILiteral>> = mutableMapOf()

    fun getSeverityId(name: String, attribute: IAttribute, workItemClient: IWorkItemClient): Any? {
        val result = priorities[name]
        return if (result == null) {
            loadSeverities(attribute, workItemClient)
            severities[name]
                ?: throw IssueClientException("Unknown priority $name")
        } else {
            result
        }
    }

    fun getSeverity(internalId: String, attribute: IAttribute, workItemClient: IWorkItemClient): Any {
        val result = severities.filterValues { it.stringIdentifier == internalId }.keys.firstOrNull()
        return if (result == null) {
            loadSeverities(attribute, workItemClient)
            severities.filterValues { it.stringIdentifier == internalId }.keys.firstOrNull()
                ?: throw IssueClientException("Unknown priority $internalId")
        } else {
            result
        }
    }

    @kotlin.jvm.Synchronized
    private fun loadSeverities(attribute: IAttribute, workItemClient: IWorkItemClient) {
        workItemClient.resolveEnumeration(attribute, null)
            .enumerationLiterals.forEach {
            severities[it.name] = it.identifier2
        }
    }

    fun getPriorityId(name: String, attribute: IAttribute, workItemClient: IWorkItemClient): Any? {
        val result = priorities[name]
        return if (result == null) {
            loadPriorities(attribute, workItemClient)
            priorities[name]
                ?: throw IssueClientException("Unknown priority $name")
        } else {
            result
        }
    }

    fun getPriorityName(internalId: String, attribute: IAttribute, workItemClient: IWorkItemClient): Any {
        val result = priorities.filterValues { it.stringIdentifier == internalId }.keys.firstOrNull()
        return if (result == null) {
            loadPriorities(attribute, workItemClient)
            priorities.filterValues { it.stringIdentifier == internalId }.keys.firstOrNull()
                ?: throw IssueClientException("Unknown priority $internalId")
        } else {
            result
        }
    }

    @kotlin.jvm.Synchronized
    private fun loadPriorities(attribute: IAttribute, workItemClient: IWorkItemClient) {
        workItemClient.resolveEnumeration(attribute, null)
            .enumerationLiterals.forEach {
            priorities[it.name] = it.identifier2
        }
    }
}