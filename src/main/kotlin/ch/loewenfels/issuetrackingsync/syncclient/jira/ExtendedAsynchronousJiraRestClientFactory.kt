package ch.loewenfels.issuetrackingsync.syncclient.jira

import com.atlassian.jira.rest.client.api.AuthenticationHandler
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import java.net.URI

class ExtendedAsynchronousJiraRestClientFactory : AsynchronousJiraRestClientFactory() {
    override fun create(
        serverUri: URI,
        authenticationHandler: AuthenticationHandler
    ): ExtendedAsynchronousJiraRestClient {
        val httpClient = AsynchronousHttpClientFactory().createClient(serverUri, authenticationHandler)
        return ExtendedAsynchronousJiraRestClient(serverUri, httpClient)
    }

    override fun createWithBasicHttpAuthentication(
        serverUri: URI,
        username: String,
        password: String
    ): ExtendedAsynchronousJiraRestClient {
        return create(serverUri, BasicHttpAuthenticationHandler(username, password))
    }
}