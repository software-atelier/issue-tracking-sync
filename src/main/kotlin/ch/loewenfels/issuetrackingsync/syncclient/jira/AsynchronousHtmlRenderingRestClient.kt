package ch.loewenfels.issuetrackingsync.syncclient.jira

import com.atlassian.httpclient.api.HttpClient
import com.atlassian.jira.rest.client.api.domain.Transition
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
            .queryParam("expand", "renderedFields")
        return getAndParse(uriBuilder.build(), RenderedFieldJsonParser(field)).claim()
    }

    override fun getAvailableTransitions(jiraKey: String): Map<Transition, String> {
        val uriBuilder = UriBuilder.fromUri(baseUri)
            .path("issue/$jiraKey/transitions")
        return getAndParse(uriBuilder.build(), ExtractTransitions()).claim()
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

    private class ExtractTransitions() : JsonObjectParser<Map<Transition, String>> {
        override fun parse(json: JSONObject?): Map<Transition, String> {
            val result = mutableMapOf<Transition, String>()
            if ((json != null) && json.has("transitions")) {
                val transitionNodes = json.getJSONArray("transitions")
                for (i in 0 until transitionNodes.length()) {
                    val transitionNode = transitionNodes.getJSONObject(i)
                    val transitionId = transitionNode.getInt("id")
                    val transitionName = transitionNode.getString("name")
                    val targetState = transitionNode.getJSONObject("to").getString("name")
                    result[Transition(transitionName, transitionId, emptyList())] = targetState
                }
            }
            return result
        }
    }
}