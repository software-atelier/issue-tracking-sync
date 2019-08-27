package ch.loewenfels.issuetrackingsync.notification

import ch.loewenfels.issuetrackingsync.Issue
import java.util.*

class NotificationObserver {
    private val channels = ArrayList<NotificationChannel>()
    fun addChannel(channel: NotificationChannel) {
        this.channels.add(channel)
    }

    fun removeChannel(channel: NotificationChannel) {
        this.channels.remove(channel)
    }

    fun notifySuccessfulSync(issue: Issue) {
        this.channels.forEach { it.onSuccessfulSync(issue) }
    }

    fun notifyException(issue: Issue, ex: Exception) {
        this.channels.forEach { it.onException(issue, ex) }
    }
}