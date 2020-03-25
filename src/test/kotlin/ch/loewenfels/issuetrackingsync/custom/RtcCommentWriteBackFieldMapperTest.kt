package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.any
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito

internal class RtcCommentWriteBackFieldMapperTest {

    @Test
    fun getValue() {
        val testee = RtcCommentWriteBackFieldMapper()
        val issueTrackingClient = Mockito.mock(IssueTrackingClient::class.java) as IssueTrackingClient<String>
        val url = "https://www.example.com/"
        val fieldname = "Remote Link:"
        Mockito.`when`(issueTrackingClient.getIssueUrl(any())).thenReturn(url)
        val result = testee.getValue("", fieldname, issueTrackingClient)
        assertEquals("$fieldname $url", result)
    }
}