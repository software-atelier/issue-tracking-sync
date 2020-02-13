package ch.loewenfels.issuetrackingsync.syncconfig

typealias TrackingApplicationName = String

data class IssueTrackingApplication(
    var className: String = "",
    var name: TrackingApplicationName = "",
    var username: String = "",
    /**
     * Should plain-text passwords in settings.json ever be an issue, look into using Jasypt
     */
    var password: String = "",
    var endpoint: String = "",
    var pollingJqlFilter: String? = null,
    var polling: Boolean = false,
    /**
     * The [project] can be a JIRA project reference (eg. 'DEV') or an RTC project area name (eg. 'Development')
     */
    var project: String? = null
)
