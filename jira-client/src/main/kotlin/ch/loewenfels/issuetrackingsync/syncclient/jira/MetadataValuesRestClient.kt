package ch.loewenfels.issuetrackingsync.syncclient.jira

interface MetadataValuesRestClient {

    /**
     * Get edit metadata of a JIRA field, such as `description`
     */
    fun getMetadataValues(jiraKey: String, field: String): Set<String>
}