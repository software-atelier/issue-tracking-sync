package ch.loewenfels.issuetrackingsync.notification

import ch.loewenfels.issuetrackingsync.Issue

interface NotificationChannel {
    fun onSuccessfulSync(issue: Issue)
    fun onException(issue: Issue, ex: Exception)
}