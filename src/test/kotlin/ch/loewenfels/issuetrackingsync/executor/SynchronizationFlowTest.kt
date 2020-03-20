package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.any
import ch.loewenfels.issuetrackingsync.executor.actions.SynchronizationAction
import ch.loewenfels.issuetrackingsync.notification.NotificationChannel
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import ch.loewenfels.issuetrackingsync.testcontext.AlwaysFalseIssueFilter
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import com.atlassian.jira.rest.client.api.RestClientException
import com.ibm.team.repository.common.TeamRepositoryException
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

internal class SynchronizationFlowTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory

    companion object TestNotificationChannel : NotificationChannel {
        val successfulIssueKeys: MutableList<String> = mutableListOf()
        val erroneousIssueKeys: MutableList<String> = mutableListOf()
        val erroneousMessages: MutableList<String> = mutableListOf()
        override fun onSuccessfulSync(
            issue: Issue,
            syncActions: Map<SyncActionName, SynchronizationAction>
        ) {
            successfulIssueKeys.add(issue.key)
        }

        override fun onException(
            issue: Issue,
            ex: Exception,
            syncActions: Map<SyncActionName, SynchronizationAction>
        ) {
            erroneousIssueKeys.add(issue.key)
            ex.message?.let { erroneousMessages.add(it) }
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
        assertEquals(1, TestNotificationChannel.erroneousIssueKeys.distinct().size)
    }

    @Test
    fun execute_jiraLoginFailureSourceIsJira_notifiedAsError() {
        // arrange
        val error = HttpStatus.UNAUTHORIZED
        val exception = RestClientException(Throwable(error.reasonPhrase), error.value())
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
        doAnswer { throw exception }//
            .`when`(sourceClient).getProprietaryIssue(anyString())
        // act
        testee.execute(issue)
        // assert
        assertThat(TestNotificationChannel.erroneousMessages, hasItem("Jira: ${error.reasonPhrase}"))
    }

    @Test
    fun execute_jiraLoginFailureSourceIsRtc_notifiedAsError() {
        // arrange
        val error = HttpStatus.UNAUTHORIZED
        val exception = RestClientException(Throwable(error.reasonPhrase), error.value())
        val syncFlowDefinition = TestObjects.buildSyncFlowDefinition("RTCCLIENT", "JIRACLIENT")
        val actionDefinitions = listOf(TestObjects.buildSyncActionDefinition())
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val notificationObserver = TestObjects.buildNotificationObserver()
        val testee =
            SynchronizationFlow(syncFlowDefinition, actionDefinitions, sourceClient, targetClient, notificationObserver)
        val issue = TestObjects.buildIssue("MK-1")
        doAnswer { throw exception }//
            .`when`(sourceClient).getProprietaryIssue(anyString())
        // act
        testee.execute(issue)
        // assert
        assertThat(TestNotificationChannel.erroneousMessages, hasItem(error.reasonPhrase))
    }

    @Test
    fun execute_rtcLoginFailureSourceIsRtc_notifiedAsError() {
        // arrange
        val error = HttpStatus.UNAUTHORIZED
        val exception = TeamRepositoryException(Throwable(error.reasonPhrase))
        val syncFlowDefinition = TestObjects.buildSyncFlowDefinition("RTCCLIENT", "JIRACLIENT")
        val actionDefinitions = listOf(TestObjects.buildSyncActionDefinition())
        val sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val notificationObserver = TestObjects.buildNotificationObserver()
        val testee =
            SynchronizationFlow(syncFlowDefinition, actionDefinitions, sourceClient, targetClient, notificationObserver)
        val issue = TestObjects.buildIssue("MK-1")
        doAnswer { throw exception }//
            .`when`(sourceClient).getProprietaryIssue(anyString())
        // act
        testee.execute(issue)
        // assert
        assertThat(TestNotificationChannel.erroneousMessages, hasItem("Rtc: ${error.reasonPhrase}"))
    }

    @Test
    fun execute_rtcLoginFailureSourceIsJira_notifiedAsError() {
        // arrange
        val error = HttpStatus.UNAUTHORIZED
        val exception = TeamRepositoryException(Throwable(error.reasonPhrase))
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
        doAnswer { throw exception }//
            .`when`(sourceClient).getProprietaryIssue(anyString())
        // act
        testee.execute(issue)
        // assert
        assertThat(TestNotificationChannel.erroneousMessages, hasItem(error.reasonPhrase))
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
            .createOrUpdateTargetIssue(any(Issue::class.java), any(DefaultsForNewIssue::class.java))
    }
}

