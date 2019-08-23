package ch.loewenfels.issuetrackingsync.client.rtc

import ch.loewenfels.issuetrackingsync.client.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.dto.Issue
import ch.loewenfels.issuetrackingsync.settings.IssueTrackingApplication
import java.time.LocalDateTime

class RtcClient(val setup: IssueTrackingApplication) : IssueTrackingClient {
    override fun changedIssuesSince(lastPollingTimestamp: LocalDateTime): Collection<Issue> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}