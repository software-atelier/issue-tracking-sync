package ch.loewenfels.issuetrackingsync.syncclient.jira

import com.atlassian.jira.rest.client.api.domain.Transition

interface HtmlRenderingRestClient {
    fun getRenderedHtml(jiraKey: String, field: String): String?

    /**
     * The JIRA REST Java Client seems quite willing to offer available transitions, but not what state
     * these transitions will result in.
     */
    fun getAvailableTransitions(jiraKey: String): Map<Transition, String>
}