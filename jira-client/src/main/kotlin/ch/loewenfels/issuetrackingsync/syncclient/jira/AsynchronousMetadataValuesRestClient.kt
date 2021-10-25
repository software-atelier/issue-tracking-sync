package ch.loewenfels.issuetrackingsync.syncclient.jira

import com.atlassian.httpclient.api.HttpClient
import com.atlassian.jira.rest.client.internal.async.AbstractAsynchronousRestClient
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser
import org.codehaus.jettison.json.JSONObject
import java.net.URI
import javax.ws.rs.core.UriBuilder

class AsynchronousMetadataValuesRestClient(private val baseUri: URI, client: HttpClient) :
  AbstractAsynchronousRestClient(client), MetadataValuesRestClient {

  override fun getMetadataValues(jiraKey: String, field: String): Set<String> {
    val uriBuilder = UriBuilder.fromUri(baseUri)
      .path("issue/$jiraKey/editmeta")
    return getAndParse(uriBuilder.build(), ExtractFieldMetadataValues(field)).claim()
  }

  private class ExtractFieldMetadataValues(private val fieldName: String) :
    JsonObjectParser<Set<String>> {
    override fun parse(json: JSONObject?): Set<String> {
      val allowedValues = HashSet<String>()
      if ((json != null) && (json.has("fields"))) {
        val jsonFields = json.getJSONObject("fields")
        if (jsonFields.has(fieldName)) {
          val jsonFieldName = jsonFields.getJSONObject(fieldName)
          if (jsonFieldName.has("allowedValues")) {
            val jsonAllowedValues = jsonFieldName.getJSONArray("allowedValues")
            for (i in 0 until jsonAllowedValues.length()) {
              val jsonAllowedValue = jsonAllowedValues.getJSONObject(i)
              val allowedValue = jsonAllowedValue.getString("value")
              allowedValues.add(allowedValue)
            }
          }
        }
      }
      return allowedValues
    }
  }
}