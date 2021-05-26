package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.any
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

internal class RtcCommentWriteBackFieldMapperTest {

    @Test
    fun getValue() {
        val testee = RtcCommentWriteBackFieldMapper()

        @Suppress("UNCHECKED_CAST")
        val issueTrackingClient = mock(IssueTrackingClient::class.java) as IssueTrackingClient<String>
        val url = "https://www.example.com/"
        val fieldname = "Remote Link:"
        `when`(issueTrackingClient.getIssueUrl(any())).thenReturn(url)
        val result = testee.getValue("", fieldname, issueTrackingClient)
        assertEquals("$fieldname $url", result)
    }
}