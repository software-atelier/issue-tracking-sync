package ch.loewenfels.issuetrackingsync.syncclient.jira

interface HtmlRenderingRestClient {
    fun getRenderedHtml(jiraKey: String, field: String): String?
}