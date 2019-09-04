package ch.loewenfels.issuetrackingsync.syncclient.rtc

import ch.loewenfels.issuetrackingsync.genericMock
import com.ibm.team.workitem.client.IWorkItemClient
import com.ibm.team.workitem.common.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito

internal class RtcMetadataTest {
    @Test
    fun getIssueTypeId() {
        // arrange
        val literals = mutableListOf(buildLiteral("foo", "1"), buildLiteral("bar", "2"))
        val enumeration = Mockito.mock(IEnumeration::class.java)
        Mockito.`when`(enumeration.enumerationLiterals).thenReturn(literals)
        val attribute = Mockito.mock(IAttribute::class.java)
        val mockClient = Mockito.mock(IWorkItemClient::class.java)
        Mockito.`when`(mockClient.resolveEnumeration(attribute, null)).thenReturn(enumeration)
        // act
        val result = RtcMetadata.getPriorityId("bar", attribute, mockClient) as Identifier<*>
        // assert
        assertEquals("2", result.stringIdentifier)
    }

    private fun buildLiteral(name: String, id: String): ILiteral {
        val identifier: Identifier<out ILiteral> = genericMock()
        Mockito.`when`(identifier.stringIdentifier).thenReturn(id)
        val result = Mockito.mock(ILiteral::class.java)
        Mockito.`when`(result.name).thenReturn(name)
        Mockito.`when`(result.identifier2).thenReturn(identifier)
        return result
    }
}