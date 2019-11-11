package ch.loewenfels.issuetrackingsync.custom

/**
 * This is an example of a custom class, ie. a class where configuration elements are coded instead of defined in a
 * JSON file. This implementation is specific to a certain JIRA / RTC setup, eeg. by using given internal state IDs
 */
class UnclosedDefectFilter : UnclosedFilter() {
    private val closedRtcStatus = listOf("ch.igs.team.workitem.workflow.change.state.s17")

    override fun getAllowedJiraIssueTypes(): List<String> = listOf("IGS Defect")

    override fun getAllowedRtcIssueTypes(): List<String> = listOf("ch.igs.team.apt.workItemType.defect")
}