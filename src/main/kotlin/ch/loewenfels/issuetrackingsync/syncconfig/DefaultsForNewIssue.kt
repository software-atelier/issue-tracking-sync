package ch.loewenfels.issuetrackingsync.syncconfig

/**
 * Defaults to be used when creating a new issue in the target system. Some of these
 * defaults might subsequently be overwritten by a [FieldMapping]
 */
data class DefaultsForNewIssue(
    var issueType: String = "",
    var project: String = "",
    var category: String = ""
)
