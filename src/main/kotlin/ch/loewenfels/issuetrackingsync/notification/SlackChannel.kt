package ch.loewenfels.issuetrackingsync.notification

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.Logging
import ch.loewenfels.issuetrackingsync.app.NotificationChannelProperties
import ch.loewenfels.issuetrackingsync.logger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.apache.http.HttpEntity
import org.apache.http.HttpHeaders
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder

class SlackChannel : NotificationChannel, Logging {
    private val timeoutInSeconds = 10
    private val objectMapper = ObjectMapper()
    private val requestConfig: RequestConfig
    private val webhookUrl: String
    private val channel: String
    private val username: String
    private val emoji: String
    // added to allow for testing
    var injectedHttpClient: CloseableHttpClient? = null

    constructor(properties: NotificationChannelProperties) {
        webhookUrl = properties.endpoint
        username = properties.username;
        channel = properties.subject
        emoji = properties.avatar
        requestConfig = RequestConfig.custom()
            .setConnectTimeout(timeoutInSeconds * 1000)
            .setConnectionRequestTimeout(timeoutInSeconds * 1000)
            .setSocketTimeout(timeoutInSeconds * 1000)
            .build();
    }

    override fun onSuccessfulSync(issue: Issue) {
        sendMessage("Successfully sync'ed issue ${issue.key}, triggered from ${issue.clientSourceName}")
        // TODO: add links using syntax (<http://localhost:4200/#/trips/4000453b-304f-435a-bad8-ad4096a97b7f|Open>)
    }

    override fun onException(issue: Issue, ex: Exception) {
        sendMessage(
            "Failed to sync issue ${issue.key} triggered from ${issue.clientSourceName}\n" +
                    "Exception was: ${ex.message}"
        )
    }

    private fun sendMessage(text: String) {
        try {
            val entity = createPostEntity(text)
            val httpPost = HttpPost(webhookUrl)
            with(httpPost) {
                this.entity = entity
                setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.mimeType)
                setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)
            }
            val client =
                injectedHttpClient ?: HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
            client.use { it.execute(httpPost) }
        } catch (ex: Exception) {
            logger().error("Failed to notify Slack", ex)
        }
    }

    private fun createPostEntity(text: String): HttpEntity {
        val payload = JsonNodeFactory.instance.objectNode();
        payload.put("text", text);
        payload.put("username", username);
        payload.put("icon_emoji", emoji);
        payload.put("channel", channel);
        return StringEntity(objectMapper.writeValueAsString(payload))
    }
}