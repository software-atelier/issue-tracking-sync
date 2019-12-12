package ch.loewenfels.issuetrackingsync.notification

import ch.loewenfels.issuetrackingsync.*
import ch.loewenfels.issuetrackingsync.app.NotificationChannelProperties
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

class SlackChannel(properties: NotificationChannelProperties) : NotificationChannel, Logging {
    private val timeoutInSeconds = 10
    private val objectMapper = ObjectMapper()
    private val requestConfig: RequestConfig
    private val webhookUrl: String = properties.endpoint
    private val channel: String = properties.subject
    private val username: String = properties.username
    private val emoji: String = properties.avatar
    // added to allow for testing
    var injectedHttpClient: CloseableHttpClient? = null

    init {
        requestConfig = RequestConfig.custom()
            .setConnectTimeout(timeoutInSeconds * 1000)
            .setConnectionRequestTimeout(timeoutInSeconds * 1000)
            .setSocketTimeout(timeoutInSeconds * 1000)
            .build()
    }

    override fun onSuccessfulSync(issue: Issue) {
        val source = issue.sourceUrl?.let { "<$it|${issue.key}>" } ?: issue.key
        val target = issue.targetUrl?.let { "<$it|${issue.targetKey ?: "Issue"}>" } ?: issue.targetKey ?: "Issue"
        val message = "Synchronized issue $source to $target\n" + issue.workLog.joinToString(separator = "\n")
        sendMessage(message.trim())
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
                injectedHttpClient ?: HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build()
            client.use { it.execute(httpPost) }
        } catch (ex: Exception) {
            logger().error("Failed to notify Slack", ex)
        }
    }

    private fun createPostEntity(text: String): HttpEntity {
        val payload = JsonNodeFactory.instance.objectNode()
        payload.put("text", text)
        payload.put("username", username)
        // If you want to use the channel override feature you need to create a webhook through the legacy
        // "Incoming Webhook" app, which you can install from the Slack App Directory.
        payload.put("icon_emoji", emoji)
        payload.put("channel", "#$channel")
        return StringEntity(objectMapper.writeValueAsString(payload), ContentType.APPLICATION_JSON)
    }
}