package ch.loewenfels.issuetrackingsync.executor.actions

import ch.loewenfels.issuetrackingsync.*
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

internal class CommentsSynchronizationActionTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory

    @Test
    fun execute_someCommentsHaveContentMatch() {
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
        val testee = CommentsSynchronizationAction()
        // act
        testee.execute(sourceClient, targetClient, issue, fieldMappings, null)
        // assert
        Mockito.verify(targetClient).addComment(safeEq(targetIssue), any(Comment::class.java))
    }

    @Test
    fun execute_targetContainsSyncedComment_nothingSynced() {
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

        Mockito.`when`(sourceClient.getComments(issue)).thenReturn(listOf(getCommentOriginal("1234")))
        Mockito.`when`(targetClient.getComments(targetIssue)).thenReturn(listOf(getCommentSynced("1234", "9999")))
        val testee = CommentsSynchronizationAction()
        // act
        testee.execute(sourceClient, targetClient, issue, fieldMappings, null)
        // assert
        Mockito.verify(targetClient, Mockito.never()).addComment(safeEq(targetIssue), any(Comment::class.java))
    }

    @Test
    fun execute_sourceContainsSyncedComment_nothingSynced() {
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

        Mockito.`when`(sourceClient.getComments(issue)).thenReturn(listOf(getCommentSynced("1234", "9999")))
        Mockito.`when`(targetClient.getComments(targetIssue)).thenReturn(listOf(getCommentOriginal("1234")))
        val testee = CommentsSynchronizationAction()
        // act
        testee.execute(sourceClient, targetClient, issue, fieldMappings, null)
        // assert
        Mockito.verify(targetClient, Mockito.never()).addComment(safeEq(targetIssue), any(Comment::class.java))
    }

    private fun getCommentOriginal(internalId: String): Comment =
        createComment("*Have* fun", internalId)

    private fun getCommentSynced(sourceInternalId: String, internalId: String): Comment =
        createComment("[from $sourceInternalId]: \n<b>Have</b> fun", internalId)

    private fun createComment(content: String, internalId: String): Comment =
        Comment("junit", LocalDateTime.now(), content, internalId)
}