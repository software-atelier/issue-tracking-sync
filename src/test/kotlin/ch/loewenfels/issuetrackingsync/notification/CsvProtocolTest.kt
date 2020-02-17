package ch.loewenfels.issuetrackingsync.notification

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.app.NotificationChannelProperties
import ch.loewenfels.issuetrackingsync.executor.SyncActionName
import ch.loewenfels.issuetrackingsync.executor.actions.SimpleSynchronizationAction
import ch.loewenfels.issuetrackingsync.executor.actions.SynchronizationAction
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDateTime

internal class CsvProtocolTest {
    @Test
    fun onSuccessfulSync_writeToCsvSuccess_noException() {
        // arrange
        val properties = defaultProperties()
        val testee = CsvProtocol(properties)
        val syncActions = createSyncActions()
        val issue = Issue("MK-1", "JIRA", LocalDateTime.now())
        // act
        testee.onSuccessfulSync(issue, syncActions)
        // assert
        assertCsvEntry(issue)
    }

    private fun defaultProperties(): NotificationChannelProperties {
        val properties = NotificationChannelProperties()
        properties.classname = "ch.loewenfels.issuetrackingsync.notification.CsvProtocol"
        properties.csvProtocolLocation = "src/test/resources/protocol.csv"
        return properties
    }

    private fun createSyncActions(): Map<SyncActionName, SynchronizationAction> {
        return mapOf<SyncActionName, SynchronizationAction>(
            Pair(
                "SynchronizeTimeJiraToRtc",
                SimpleSynchronizationAction("CSV")
            )
        )
    }

    private fun assertCsvEntry(issue: Issue) {
        val file: File = File(defaultProperties().csvProtocolLocation)
        val lastLine = file.readLines()
            .findLast { line -> line.contains(issue.key) }
        assertThat(lastLine, containsString("SynchronizeTimeJiraToRtc"))
    }
}
