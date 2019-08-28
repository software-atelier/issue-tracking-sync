package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired

internal class SyncChangesActionTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory

    @Test
    fun execute() {
        // arrange
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        val keyFieldMapping = TestObjects.buildKeyFieldMapping()
        val fieldMappings = TestObjects.buildFieldMappingList()
        val issue = sourceClient.getIssue("MK-1") ?: throw IllegalArgumentException("Unknown key")
        val testee = SyncChangesAction()
        // act
        testee.execute(sourceClient, targetClient, issue, keyFieldMapping, fieldMappings, null)
        // assert
        assertNotNull(issue.proprietarySourceInstance)

        verify(targetClient).createOrUpdateTargetIssue(issue, null)
    }
}