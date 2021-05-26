package ch.loewenfels.issuetrackingsync.syncclient.rtc

import ch.loewenfels.issuetrackingsync.genericMock
import com.ibm.team.workitem.client.IWorkItemClient
import com.ibm.team.workitem.common.model.IAttribute
import com.ibm.team.workitem.common.model.IEnumeration
import com.ibm.team.workitem.common.model.ILiteral
import com.ibm.team.workitem.common.model.Identifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

internal class RtcMetadataTest {
    @Test
    fun getIssueTypeId() {
        // arrange
        val literals = mutableListOf(buildLiteral("foo", "1"), buildLiteral("bar", "2"))
        val enumeration = mock(IEnumeration::class.java)
        `when`(enumeration.enumerationLiterals).thenReturn(literals)
        val attribute = mock(IAttribute::class.java)
        val mockClient = mock(IWorkItemClient::class.java)
        `when`(mockClient.resolveEnumeration(attribute, null)).thenReturn(enumeration)
        // act
        val result = RtcMetadata.getPriorityId("bar", attribute, mockClient) as Identifier<*>
        // assert
        assertEquals("2", result.stringIdentifier)
    }

    private fun buildLiteral(name: String, id: String): ILiteral {
        val identifier: Identifier<out ILiteral> = genericMock()
        `when`(identifier.stringIdentifier).thenReturn(id)
        val result = mock(ILiteral::class.java)
        `when`(result.name).thenReturn(name)
        `when`(result.identifier2).thenReturn(identifier)
        return result
    }
}