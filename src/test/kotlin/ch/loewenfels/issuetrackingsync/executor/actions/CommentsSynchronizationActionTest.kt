package ch.loewenfels.issuetrackingsync.executor.actions

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.Comment
import ch.loewenfels.issuetrackingsync.any
import ch.loewenfels.issuetrackingsync.safeEq
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncconfig.AdditionalProperties
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

internal class CommentsSynchronizationActionTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory


    @Test
    fun execute_someCommentsHaveContentFilterFiltersEverything_NoSync() {
        // arrange
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        val fieldMappings = TestObjects.buildFieldMappingList()
        val issue = sourceClient.getIssue("MK-1") ?: throw IllegalArgumentException("Unknown key")
        val targetIssue = targetClient.getIssue("1234") ?: throw IllegalArgumentException("Unknown key")
        val additionalProperties = AdditionalProperties()
        additionalProperties.commentFilter =
            listOf(ch.loewenfels.issuetrackingsync.syncconfig.CommentFilter("ch.loewenfels.issuetrackingsync.executor.actions.CommentFilterAlwaysFalseStub"))
        issue.proprietarySourceInstance = issue
        issue.proprietaryTargetInstance = targetIssue
        val testee = CommentsSynchronizationAction()
        // act
        testee.execute(sourceClient, targetClient, issue, fieldMappings, null, additionalProperties)
        // assert
        Mockito.verify(targetClient, never()).addComment(safeEq(targetIssue), any(Comment::class.java))
    }

    @Test
    fun execute_someCommentsHaveContentFilterFiltersNothing_CommentsShouldGetSynced() {
        // arrange
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        val fieldMappings = TestObjects.buildFieldMappingList()
        val issue = sourceClient.getIssue("MK-1") ?: throw IllegalArgumentException("Unknown key")
        val targetIssue = targetClient.getIssue("1234") ?: throw IllegalArgumentException("Unknown key")
        val additionalProperties = AdditionalProperties()
        additionalProperties.commentFilter =
            listOf(ch.loewenfels.issuetrackingsync.syncconfig.CommentFilter("ch.loewenfels.issuetrackingsync.executor.actions.CommentFilterAlwaysTrueStub"))
        issue.proprietarySourceInstance = issue
        issue.proprietaryTargetInstance = targetIssue
        val testee = CommentsSynchronizationAction()
        // act
        testee.execute(sourceClient, targetClient, issue, fieldMappings, null, additionalProperties)
        // assert
        Mockito.verify(targetClient).addComment(safeEq(targetIssue), any(Comment::class.java))
    }

    @Test
    fun execute_commentsAreBeforeCreatedAfterOfFilter_NoSync() {
        // arrange
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        val fieldMappings = TestObjects.buildFieldMappingList()
        val issue = sourceClient.getIssue("MK-1") ?: throw IllegalArgumentException("Unknown key")
        val targetIssue = targetClient.getIssue("1234") ?: throw IllegalArgumentException("Unknown key")
        val additionalProperties = AdditionalProperties()
        additionalProperties.commentFilter =
            listOf(ch.loewenfels.issuetrackingsync.syncconfig.CommentFilter("ch.loewenfels.issuetrackingsync.custom.CreatedBeforeCommentFilter", mapOf(
                Pair("createdBefore", LocalDateTime.now().toString()))))
        issue.proprietarySourceInstance = issue
        issue.proprietaryTargetInstance = targetIssue
        val testee = CommentsSynchronizationAction()
        // act
        testee.execute(sourceClient, targetClient, issue, fieldMappings, null, additionalProperties)
        // assert
        Mockito.verify(targetClient, never()).addComment(safeEq(targetIssue), any(Comment::class.java))
    }

    @Test
    fun execute_commentsAreAfterCreatedAfterOfFilter_CommentsShouldGetSynced() {
        // arrange
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        val fieldMappings = TestObjects.buildFieldMappingList()
        val issue = sourceClient.getIssue("MK-1") ?: throw IllegalArgumentException("Unknown key")
        val targetIssue = targetClient.getIssue("1234") ?: throw IllegalArgumentException("Unknown key")
        val additionalProperties = AdditionalProperties()
        additionalProperties.commentFilter =
            listOf(ch.loewenfels.issuetrackingsync.syncconfig.CommentFilter("ch.loewenfels.issuetrackingsync.custom.CreatedBeforeCommentFilter", mapOf(
                Pair("createdBefore", LocalDateTime.now().minusHours(48).toString())
            )))
        issue.proprietarySourceInstance = issue
        issue.proprietaryTargetInstance = targetIssue
        val testee = CommentsSynchronizationAction()
        // act
        testee.execute(sourceClient, targetClient, issue, fieldMappings, null, additionalProperties)
        // assert
        Mockito.verify(targetClient).addComment(safeEq(targetIssue), any(Comment::class.java))
    }

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

class CommentFilterAlwaysFalseStub(val filterProperties: Map<String, String> = emptyMap()) : CommentFilter {
    override fun getFilter(): (Comment) -> Boolean = { (_) -> false }

}

class CommentFilterAlwaysTrueStub(val filterProperties: Map<String, String> = emptyMap()) : CommentFilter {
    override fun getFilter(): (Comment) -> Boolean = { (_) -> true }

}
