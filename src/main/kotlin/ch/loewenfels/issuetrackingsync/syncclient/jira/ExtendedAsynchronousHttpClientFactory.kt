package ch.loewenfels.issuetrackingsync.syncclient.jira

import com.atlassian.event.api.EventPublisher
import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClientFactory
import com.atlassian.httpclient.api.HttpClient
import com.atlassian.httpclient.api.factory.HttpClientOptions
import com.atlassian.jira.rest.client.api.AuthenticationHandler
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory
import com.atlassian.jira.rest.client.internal.async.AtlassianHttpClientDecorator
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient
import com.atlassian.sal.api.ApplicationProperties
import com.atlassian.sal.api.UrlMode
import com.atlassian.sal.api.executor.ThreadLocalContextManager
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.Nonnull

class ExtendedAsynchronousHttpClientFactory : AsynchronousHttpClientFactory() {

    fun createClient(
            serverUri: URI,
            authenticationHandler: AuthenticationHandler,
            socketTimeout: Int
    ): DisposableHttpClient {
        val options = HttpClientOptions()
        options.setSocketTimeout(socketTimeout, TimeUnit.MILLISECONDS)

        return createHttpClient(serverUri, authenticationHandler, options)
    }

    private fun createHttpClient(serverUri: URI,
                                 authenticationHandler: AuthenticationHandler,
                                 options: HttpClientOptions
    ): AtlassianHttpClientDecorator {
        val defaultHttpClientFactory: DefaultHttpClientFactory<*> = DefaultHttpClientFactory(NoOpEventPublisher(),
                RestClientApplicationProperties(serverUri),
                object : ThreadLocalContextManager<Any?> {
                    override fun getThreadLocalContext(): Any? {
                        return null
                    }

                    override fun setThreadLocalContext(context: Any?) {}
                    override fun clearThreadLocalContext() {}
                })
        val httpClient = defaultHttpClientFactory.create(options)
        return object : AtlassianHttpClientDecorator(httpClient, authenticationHandler) {
            @Throws(Exception::class)
            override fun destroy() {
                defaultHttpClientFactory.dispose(httpClient)
            }
        }
    }

    private class NoOpEventPublisher : EventPublisher {
        override fun publish(o: Any) {}
        override fun register(o: Any) {}
        override fun unregister(o: Any) {}
        override fun unregisterAll() {}
    }

    /**
     * These properties are used to present JRJC as a User-Agent during http requests.
     */
    private class RestClientApplicationProperties(jiraURI: URI) : ApplicationProperties {
        private val baseUrl: String = jiraURI.path
        override fun getBaseUrl(): String {
            return baseUrl
        }

        /**
         * We'll always have an absolute URL as a client.
         */
        @Nonnull
        override fun getBaseUrl(urlMode: UrlMode): String {
            return baseUrl
        }

        @Nonnull
        override fun getDisplayName(): String {
            return "Atlassian JIRA Rest Java Client"
        }

        @Nonnull
        override fun getPlatformId(): String {
            return ApplicationProperties.PLATFORM_JIRA
        }

        @Nonnull
        override fun getVersion(): String {
            return MavenUtils.getVersion("com.atlassian.jira", "jira-rest-java-com.atlassian.jira.rest.client")
        }

        @Nonnull
        override fun getBuildDate(): Date {
            // TODO implement using MavenUtils, JRJC-123
            throw UnsupportedOperationException()
        }

        @Nonnull
        override fun getBuildNumber(): String {
            // TODO implement using MavenUtils, JRJC-123
            return 0.toString()
        }

        override fun getHomeDirectory(): File? {
            return File(".")
        }

        override fun getPropertyValue(s: String): String {
            throw UnsupportedOperationException("Not implemented")
        }

    }

    private object MavenUtils {
        private val logger = LoggerFactory.getLogger(MavenUtils::class.java)
        private const val UNKNOWN_VERSION = "unknown"
        fun getVersion(groupId: String?, artifactId: String?): String {
            val props = Properties()
            var resourceAsStream: InputStream? = null
            return try {
                resourceAsStream = MavenUtils::class.java.getResourceAsStream(String.format("/META-INF/maven/%s/%s/pom.properties", groupId, artifactId))
                props.load(resourceAsStream)
                props.getProperty("version", UNKNOWN_VERSION)
            } catch (e: Exception) {
                logger.debug("Could not find version for maven artifact {}:{}", groupId, artifactId)
                logger.debug("Got the following exception", e)
                UNKNOWN_VERSION
            } finally {
                if (resourceAsStream != null) {
                    try {
                        resourceAsStream.close()
                    } catch (ioe: IOException) {
                        // ignore
                    }
                }
            }
        }
    }
}