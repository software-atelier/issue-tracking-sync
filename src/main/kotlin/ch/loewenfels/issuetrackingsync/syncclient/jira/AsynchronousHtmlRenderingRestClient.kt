package ch.loewenfels.issuetrackingsync.syncclient.jira

import com.atlassian.httpclient.api.HttpClient
import com.atlassian.jira.rest.client.internal.async.AbstractAsynchronousRestClient
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser
import org.codehaus.jettison.json.JSONObject
import java.net.URI
import javax.ws.rs.core.UriBuilder

class AsynchronousHtmlRenderingRestClient(private val baseUri: URI, client: HttpClient) :
    AbstractAsynchronousRestClient(client), HtmlRenderingRestClient {
    override fun getRenderedHtml(jiraKey: String, field: String): String? {
        val uriBuilder = UriBuilder.fromUri(baseUri)
            .path("issue/$jiraKey")
            .queryParam("expand", "renderedFields");
        return getAndParse(uriBuilder.build(), RenderedFieldJsonParser(field)).claim()
    }

    private class RenderedFieldJsonParser(private val fieldName: String) : JsonObjectParser<String> {
        override fun parse(json: JSONObject?): String {
            if ((json == null) || (!json.has("renderedFields"))) {
                return ""
            }
            val renderedFields = json.getJSONObject("renderedFields")
            if (!renderedFields.has(fieldName)) {
                return ""
            }
            return renderedFields.getString(fieldName)
        }
    }
}