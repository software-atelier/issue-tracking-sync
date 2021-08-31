package ch.loewenfels.issuetrackingsync.syncclient.file

import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class FileClientTest {

    @Test
    fun getIssue_readFromFile_deserialized() {
        // arrange
        val testee = createTestee()
        // act
        val issue = testee.getIssue("Test-1")!!
        // assert
        Assertions.assertEquals("Test-1", issue.key)
        Assertions.assertEquals("MrDolch", issue.lastUpdatedBy)
        Assertions.assertEquals(LocalDateTime.of(2021, 8, 26, 7, 19, 0), issue.lastUpdated)
    }

    @Test
    fun getIssue_FileIssueNotExists_null() {
        // arrange
        val testee = createTestee()
        // act
        val issue = testee.getIssue("FileIssue-NotExists")
        // assert
        Assertions.assertNull(issue)
    }

    private fun createTestee(): FileClient {
        val setup = IssueTrackingApplication()
        setup.endpoint = "src/test/resources/FileIssues"
        setup.className = FileClient::class.qualifiedName!!
        val testee = FileClient(setup)
        return testee
    }
}