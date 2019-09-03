package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.*
import ch.loewenfels.issuetrackingsync.notification.NotificationChannel
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import ch.loewenfels.issuetrackingsync.testcontext.AlwaysFalseIssueFilter
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.springframework.beans.factory.annotation.Autowired

internal class SynchronizationFlowTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory

    companion object TestNotificationChannel : NotificationChannel {
        val successfulIssueKeys: MutableList<String> = mutableListOf()
        val erroneousIssueKeys: MutableList<String> = mutableListOf()
        override fun onSuccessfulSync(issue: Issue) {
            successfulIssueKeys.add(issue.key)
        }

        override fun onException(issue: Issue, ex: Exception) {
            erroneousIssueKeys.add(issue.key)
        }
    }

    @Test
    fun applies_validClientName_true() {
        // arrange
        val syncFlowDefinition = TestObjects.buildSyncFlowDefinition("JIRACLIENT", "RTCCLIENT")
        val actionDefinitions = listOf(TestObjects.buildSyncActionDefinition())
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        val notificationObserver = TestObjects.buildNotificationObserver()
        val testee =
            SynchronizationFlow(syncFlowDefinition, actionDefinitions, sourceClient, targetClient, notificationObserver)
        val issue = TestObjects.buildIssue()
        // act
        val result = testee.applies(syncFlowDefinition.source, issue)
        // assert
        assertTrue(result)
    }

    @Test
    fun applies_validClientNameFilterNotMatched_false() {
        // arrange
        val syncFlowDefinition = TestObjects.buildSyncFlowDefinition("JIRACLIENT", "RTCCLIENT")
        val actionDefinitions = listOf(TestObjects.buildSyncActionDefinition())
        syncFlowDefinition.filterClassname = AlwaysFalseIssueFilter::class.qualifiedName ?: ""
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        val notificationObserver = TestObjects.buildNotificationObserver()
        val testee =
            SynchronizationFlow(syncFlowDefinition, actionDefinitions, sourceClient, targetClient, notificationObserver)
        val issue = TestObjects.buildIssue()
        // act
        val result = testee.applies(syncFlowDefinition.source, issue)
        // assert
        assertFalse(result)
    }

    @Test
    fun applies_invalidClientName_false() {
        // arrange
        val syncFlowDefinition = TestObjects.buildSyncFlowDefinition("JIRACLIENT", "RTCCLIENT")
        val actionDefinitions = listOf(TestObjects.buildSyncActionDefinition())
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        val notificationObserver = TestObjects.buildNotificationObserver()
        val testee =
            SynchronizationFlow(syncFlowDefinition, actionDefinitions, sourceClient, targetClient, notificationObserver)
        val issue = TestObjects.buildIssue()
        // act
        val result = testee.applies("foobar", issue)
        // assert
        assertFalse(result)
    }

    @Test
    fun execute_issueUpdatedInTheMeantime_notifiedAsError() {
        // arrange
        val syncFlowDefinition = TestObjects.buildSyncFlowDefinition("JIRACLIENT", "RTCCLIENT")
        val actionDefinitions = listOf(TestObjects.buildSyncActionDefinition())
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        val notificationObserver = TestObjects.buildNotificationObserver()
        val testee =
            SynchronizationFlow(syncFlowDefinition, actionDefinitions, sourceClient, targetClient, notificationObserver)
        val issue = TestObjects.buildIssue("MK-1")
        // act
        testee.execute(issue)
        // assert
        assertEquals(1, TestNotificationChannel.erroneousIssueKeys.size)
    }

    @Test
    fun execute_validKey_syncNotifiedSuccessful() {
        // arrange
        val syncFlowDefinition = TestObjects.buildSyncFlowDefinition("JIRACLIENT", "RTCCLIENT")
        val actionDefinitions = listOf(TestObjects.buildSyncActionDefinition())
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        val notificationObserver = TestObjects.buildNotificationObserver()
        val testee =
            SynchronizationFlow(syncFlowDefinition, actionDefinitions, sourceClient, targetClient, notificationObserver)
        val issue = sourceClient.getIssue("MK-1") ?: throw IllegalArgumentException("Unknown key")
        // act
        testee.execute(issue)
        // assert
        assertEquals(1, TestNotificationChannel.successfulIssueKeys.size)
        Mockito.verify(sourceClient, times(1))
            .createOrUpdateTargetIssue(any(Issue::class.java), any(DefaultsForNewIssue::class.java))
        Mockito.verify(targetClient, times(2))
            .createOrUpdateTargetIssue(safeEq(issue), any(DefaultsForNewIssue::class.java))
    }
}

