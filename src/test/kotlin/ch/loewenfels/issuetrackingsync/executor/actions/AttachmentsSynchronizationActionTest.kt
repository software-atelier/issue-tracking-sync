package ch.loewenfels.issuetrackingsync.executor.actions

import ch.loewenfels.issuetrackingsync.*
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired

internal class AttachmentsSynchronizationActionTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory

    @Test
    fun execute() {
        // arrange
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        val fieldMappings = TestObjects.buildFieldMappingList()
        val issue = sourceClient.getIssue("MK-1") ?: throw IllegalArgumentException("Unknown key")
        val targetIssue = targetClient.getIssue("1234") ?: throw IllegalArgumentException("Unknown key")
        issue.proprietarySourceInstance = issue
        issue.proprietaryTargetInstance = targetIssue
        val testee = AttachmentsSynchronizationAction()
        // act
        testee.execute(sourceClient, targetClient, issue, fieldMappings, null)
        // assert
        Mockito.verify(targetClient).addAttachment(safeEq(targetIssue), any(Attachment::class.java))
    }
}