package ch.loewenfels.issuetrackingsync.settings

data class IssueTrackingApplication(
    var className: String? = null,
    var name: String? = null,
    var username: String? = null,
    var password: String? = null,
    var endpoint: String? = null
)
