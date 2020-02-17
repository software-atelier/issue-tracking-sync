package ch.loewenfels.issuetrackingsync.notification

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.app.NotificationChannelProperties
import ch.loewenfels.issuetrackingsync.executor.SyncActionName
import ch.loewenfels.issuetrackingsync.executor.actions.SynchronizationAction
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.time.LocalDateTime


class CsvProtocol(properties: NotificationChannelProperties) : NotificationChannel {
    private val csvHeader = "Datum;Source Issue;Target Issue;Sync Actions;Status"
    private val csvLocation = properties.csvProtocolLocation
    private val file = File(csvLocation)
    private val fos = FileOutputStream(file, true);
    private val bw = BufferedWriter(OutputStreamWriter(fos))

    init {
        if (file.readLines().size == 0) {
            bw.appendln(csvHeader)
            bw.flush()
        }
    }

    override fun onSuccessfulSync(
        issue: Issue,
        syncActions: Map<SyncActionName, SynchronizationAction>
    ) {
        writeToCsv(issue, syncActions, true)
    }

    override fun onException(
        issue: Issue,
        ex: Exception,
        syncActions: Map<SyncActionName, SynchronizationAction>
    ) {
        writeToCsv(issue, syncActions, false)
    }

    private fun writeToCsv(
        issue: Issue,
        syncActions: Map<SyncActionName, SynchronizationAction>,
        success: Boolean
    ) {
        val source = issue.key
        val target = issue.targetKey ?: ""
        val status = if (success) "Erfolgreich" else "Fehler"
        val entry = "${LocalDateTime.now()};${source};${target};${concatActions(syncActions)};$status"
        bw.appendln(entry)
        bw.flush()
    }

    private fun concatActions(syncActions: Map<SyncActionName, SynchronizationAction>) =
        syncActions.keys.toList().toString().replace("[", "").replace("]", "")
}