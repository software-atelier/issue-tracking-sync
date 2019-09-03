package ch.loewenfels.issuetrackingsync.syncclient.jira

import ch.loewenfels.issuetrackingsync.syncclient.IssueClientException
import com.atlassian.jira.rest.client.api.JiraRestClient

object JiraMetadata {
    private val issueTypes: MutableMap<String, Long> = mutableMapOf()
    private val priorities: MutableMap<String, Long> = mutableMapOf()
    private val fieldTypes: MutableMap<String, String> = mutableMapOf()

    fun getIssueTypeId(name: String, jiraRestClient: JiraRestClient): Long {
        var result = name.toLongOrNull() ?: issueTypes[name]
        return if (result == null) {
            loadIssueTypes(jiraRestClient)
            name.toLongOrNull() ?: issueTypes[name]
            ?: throw IssueClientException("Unknown issue type $name")
        } else {
            result
        }
    }

    @kotlin.jvm.Synchronized
    private fun loadIssueTypes(jiraRestClient: JiraRestClient) {
        jiraRestClient.metadataClient.issueTypes.claim().forEach {
            issueTypes[it.name] = it.id
        }
    }

    fun getPriorityId(name: String, jiraRestClient: JiraRestClient): Long {
        var result = name.toLongOrNull() ?: priorities[name]
        return if (result == null) {
            loadPriorities(jiraRestClient)
            name.toLongOrNull() ?: priorities[name]
            ?: throw IssueClientException("Unknown priority $name")
        } else {
            result
        }
    }

    fun getPriorityName(internalId: Long, jiraRestClient: JiraRestClient): String {
        var result = priorities.filterValues { it == internalId }.keys.firstOrNull()
        return if (result == null) {
            loadPriorities(jiraRestClient)
            priorities.filterValues { it == internalId }.keys.firstOrNull()
                ?: throw IssueClientException("Unknown priority $internalId")
        } else {
            result
        }
    }

    @kotlin.jvm.Synchronized
    private fun loadPriorities(jiraRestClient: JiraRestClient) {
        jiraRestClient.metadataClient.priorities.claim().forEach {
            priorities[it.name] = it.id ?: 0L
        }
    }

    fun getFieldType(internalId: String, jiraRestClient: JiraRestClient): String {
        var result = fieldTypes[internalId]
        return if (result == null) {
            loadFieldTypes(jiraRestClient)
            fieldTypes[internalId] ?: throw IssueClientException("Unknown field $internalId")
        } else {
            result
        }
    }

    @kotlin.jvm.Synchronized
    private fun loadFieldTypes(jiraRestClient: JiraRestClient) {
        jiraRestClient.metadataClient.fields.claim().forEach { f ->
            fieldTypes[f.id] = f.schema?.type ?: ""
        }
    }
}