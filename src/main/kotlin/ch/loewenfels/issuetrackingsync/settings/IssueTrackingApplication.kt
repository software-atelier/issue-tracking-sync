package ch.loewenfels.issuetrackingsync.settings

typealias TrackingApplicationName = String

enum class ApplicationRole() {
    /**
     * Creating a system in a PEER will sync to all PEER and SLAVE
     */
    PEER,
    /**
     * Creating a system in a MASTER will sync to all PEER and SLAVE
     */
    MASTER,
    /**
     * Creating a system in a SLAVE  will NOT sync anywhere
     */
    SLAVE
}

data class IssueTrackingApplication(
    var className: String = "",
    var role: ApplicationRole = ApplicationRole.PEER,
    var name: String = "",
    var username: String = "",
    /**
     * Should plain-text passwords in settings.json ever be an issue, look into using Jasypt
     */
    var password: String = "",
    var endpoint: String = "",
    var polling: Boolean = false,
    /**
     * If this map is not empty the defined fields will be used as "not empty" when polling for issues.
     * For a JIRA custom field, only define the numeric ID (ie. NOT the "customfield_" prefix)
     */
    var fieldsHoldingPartnerApplicationKey: Map<TrackingApplicationName, String> = mapOf(),
    var rtcProjectArea: String? = null
)
