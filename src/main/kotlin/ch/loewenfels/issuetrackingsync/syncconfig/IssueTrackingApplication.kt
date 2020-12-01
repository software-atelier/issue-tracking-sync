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
    var pollingIssueType: String? = null,
    var polling: Boolean = false,
    /**
     * The [project] can be a JIRA project reference (eg. 'DEV') or an RTC project area name (eg. 'Development')
     */
    var project: String? = null,
    var extRefIdField: String = "",
    var extRefIdFieldPattern: String? = null,
    /**
     * The external reference field can contain additional informations, so prepare if necessary
     */
    var proprietaryIssueQueryBuilder: String? = null,
    /**
     * JIRA has default value of socket timeout (20.000 milliseconds)
     */
    var socketTimeout: Int? = null,

    var log: LogDefinition? = null
)
