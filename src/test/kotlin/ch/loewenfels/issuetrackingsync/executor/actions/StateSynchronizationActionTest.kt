package ch.loewenfels.issuetrackingsync.executor.actions

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.any
import ch.loewenfels.issuetrackingsync.safeEq
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncconfig.AdditionalProperties
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.anyList
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

internal class StateSynchronizationActionTest : AbstractSpringTest() {
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
        Mockito.`when`(targetClient.getLastUpdated(targetIssue)).thenReturn(LocalDateTime.MIN)
        issue.proprietarySourceInstance = issue
        issue.proprietaryTargetInstance = targetIssue
        val testee = StateSynchronizationAction()
        // act
        testee.execute(sourceClient, targetClient, issue, fieldMappings, null, AdditionalProperties())
        // assert
        verify(targetClient).setState(safeEq(targetIssue), any(String::class.java), anyList())
    }
}