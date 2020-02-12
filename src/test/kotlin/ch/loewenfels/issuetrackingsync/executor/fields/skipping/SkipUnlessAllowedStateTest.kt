package ch.loewenfels.issuetrackingsync.executor.fields.skipping

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncconfig.FieldSkippingEvaluatorDefinition
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class SkipUnlessAllowedStateTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory

    @Test
    fun hasFieldToBeSkipped_allowedState_fieldShouldNotBeSkipped() {
        val fieldSkipDefinition = FieldSkippingEvaluatorDefinition(
            SkipUnlessAllowedState::class.java.name,
            mutableMapOf("allowedStates" to mutableListOf("In work"))
        )
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        issue.proprietaryTargetInstance = issue
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val testee = SkipUnlessAllowedState(fieldSkipDefinition)
        // act
        val result = testee.hasFieldToBeSkipped(targetClient, issue, issue, "wayne")
        // assert
        Assertions.assertFalse(result)
    }

    @Test
    fun hasFieldToBeSkipped_notAllowedState_fieldShouldNotBeSkipped() {
        val fieldSkipDefinition = FieldSkippingEvaluatorDefinition(
            SkipUnlessAllowedState::class.java.name,
            mutableMapOf("allowedStates" to mutableListOf("someOtherState"))
        )
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        issue.proprietaryTargetInstance = issue
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val testee = SkipUnlessAllowedState(fieldSkipDefinition)
        // act
        val result = testee.hasFieldToBeSkipped(targetClient, issue, issue, "wayne")
        // assert
        Assertions.assertTrue(result)
    }

    @Test
    fun hasFieldToBeSkipped_allowedStatesIsNotList_illegalStateException() {
        val fieldSkipDefinition = FieldSkippingEvaluatorDefinition(
            SkipUnlessAllowedState::class.java.name,
            mutableMapOf("allowedStates" to "someOtherState")
        )
        val issue = TestObjects.buildIssue("MK-1")
        issue.sourceUrl = "http://localhost/issues/MK-1"
        issue.proprietaryTargetInstance = issue
        val targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        val testee = SkipUnlessAllowedState(fieldSkipDefinition)
        // act
        Assertions.assertThrows(IllegalStateException::class.java) {
            testee.hasFieldToBeSkipped(
                targetClient,
                issue,
                issue,
                "wayne"
            )
        }
    }
}
