package ch.loewenfels.issuetrackingsync.syncclient.jira

import ch.loewenfels.issuetrackingsync.syncclient.IssueClientException
import com.atlassian.jira.rest.client.api.JiraRestClient

object JiraMetadata {
    private val issueTypes: MutableMap<String, Long> = mutableMapOf()
    private val priorities: MutableMap<String, Long> = mutableMapOf()
    private val fieldTypes: MutableMap<String, String> = mutableMapOf()

    fun getIssueTypeId(name: String, jiraRestClient: JiraRestClient): Long {
        return getId(issueTypes, "issue type", name, jiraRestClient)
    }

    fun getPriorityId(name: String, jiraRestClient: JiraRestClient): Long {
        return getId(priorities, "priority", name, jiraRestClient)
    }

    private fun getId(
        collection: MutableMap<String, Long>,
        property: String,
        name: String,
        jiraRestClient: JiraRestClient
    ): Long {
        val result = name.toLongOrNull() ?: collection[name]
        return if (result == null) {
            loadIssueTypes(jiraRestClient)
            loadPriorities(jiraRestClient)
            name.toLongOrNull() ?: collection[name]
            ?: throw IssueClientException("Unknown $property $name")
        } else {
            result
        }
    }

    fun getPriorityName(internalId: Long, jiraRestClient: JiraRestClient): String {
        return getName(priorities, "priority", null, internalId, jiraRestClient)
    }

    fun getFieldType(internalId: String, jiraRestClient: JiraRestClient): String {
        return getName(fieldTypes, "field", internalId, null, jiraRestClient)
    }

    private fun getName(
        collection: MutableMap<*, *>,
        property: String,
        internalName: String?,
        internalId: Long?,
        jiraRestClient: JiraRestClient
    ): String {
        val result = if (internalName != null) {
            collection[internalName]
        } else {
            collection.filterValues { it == internalId }.keys.firstOrNull()
        }
        return if (result == null) {
            loadPriorities(jiraRestClient)
            loadFieldTypes(jiraRestClient)
            collection.filterValues { it == internalId }.keys.firstOrNull() as String?
                ?: throw IssueClientException("Unknown $property $internalId")
        } else {
            result as String
        }
    }

    @kotlin.jvm.Synchronized
    private fun loadIssueTypes(jiraRestClient: JiraRestClient) {
        jiraRestClient.metadataClient.issueTypes.claim().forEach {
            issueTypes[it.name] = it.id
        }
    }

    @kotlin.jvm.Synchronized
    private fun loadPriorities(jiraRestClient: JiraRestClient) {
        jiraRestClient.metadataClient.priorities.claim().forEach {
            priorities[it.name] = it.id ?: 0L
        }
    }

    @kotlin.jvm.Synchronized
    private fun loadFieldTypes(jiraRestClient: JiraRestClient) {
        jiraRestClient.metadataClient.fields.claim().forEach { f ->
            fieldTypes[f.id] = f.schema?.type ?: ""
        }
    }
}