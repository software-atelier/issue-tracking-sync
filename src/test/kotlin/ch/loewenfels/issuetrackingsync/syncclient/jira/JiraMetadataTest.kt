package ch.loewenfels.issuetrackingsync.syncclient.jira

import ch.loewenfels.issuetrackingsync.testcontext.genericMock
import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.MetadataRestClient
import com.atlassian.jira.rest.client.api.domain.IssueType
import com.atlassian.jira.rest.client.api.domain.Priority
import io.atlassian.util.concurrent.Promise
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.net.URI

internal class JiraMetadataTest {
    private val someUri = URI.create("")

    @Test
    fun getIssueTypeId() {
        // arrange
        val issueType = listOf(IssueType(someUri, 123L, "Defect", false, "", someUri))
        val issueTypesPromise: Promise<Iterable<IssueType>> = genericMock()
        Mockito.`when`(issueTypesPromise.claim()).thenReturn(issueType)
        val metadataClient = Mockito.mock(MetadataRestClient::class.java)
        Mockito.`when`(metadataClient.issueTypes).thenReturn(issueTypesPromise)
        val mockClient = Mockito.mock(JiraRestClient::class.java)
        Mockito.`when`(mockClient.metadataClient).thenReturn(metadataClient)
        // act
        val result = JiraMetadata.getIssueTypeId("Defect", mockClient)
        // assert
        assertEquals(123L, result)
    }

    @Test
    fun getPriorityId() {
        // arrange
        val priorities = listOf(Priority(someUri, 123L, "Hoch", "Orange", "", someUri))
        val prioritiesPromise: Promise<Iterable<Priority>> = genericMock()
        Mockito.`when`(prioritiesPromise.claim()).thenReturn(priorities)
        val metadataClient = Mockito.mock(MetadataRestClient::class.java)
        Mockito.`when`(metadataClient.priorities).thenReturn(prioritiesPromise)
        val mockClient = Mockito.mock(JiraRestClient::class.java)
        Mockito.`when`(mockClient.metadataClient).thenReturn(metadataClient)
        // act
        val result = JiraMetadata.getPriorityId("Hoch", mockClient)
        // assert
        assertEquals(123L, result)
    }

    @Test
    fun getPriorityName() {
        // arrange
        val priorities = listOf(Priority(someUri, 123L, "Hoch", "Orange", "", someUri))
        val prioritiesPromise: Promise<Iterable<Priority>> = genericMock()
        Mockito.`when`(prioritiesPromise.claim()).thenReturn(priorities)
        val metadataClient = Mockito.mock(MetadataRestClient::class.java)
        Mockito.`when`(metadataClient.priorities).thenReturn(prioritiesPromise)
        val mockClient = Mockito.mock(JiraRestClient::class.java)
        Mockito.`when`(mockClient.metadataClient).thenReturn(metadataClient)
        // act
        val result = JiraMetadata.getPriorityName(123L, mockClient)
        // assert
        assertEquals("Hoch", result)
    }
}

