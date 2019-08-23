package ch.loewenfels.issuetrackingsync.settings

data class IssueTrackingApplication(
    var className: String = "",
    var name: String = "",
    var username: String = "",
    var password: String = "",
    var endpoint: String = "",
    var polling: Boolean = false
)
