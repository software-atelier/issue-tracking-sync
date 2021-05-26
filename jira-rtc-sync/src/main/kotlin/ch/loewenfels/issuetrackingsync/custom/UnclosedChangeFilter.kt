package ch.loewenfels.issuetrackingsync.custom

class UnclosedChangeFilter : UnclosedFilter() {

    override fun getAllowedJiraIssueTypes(): List<String> = listOf("IGS Change")

    override fun getAllowedRtcIssueTypes(): List<String> = listOf("ch.igs.team.apt.workItemType.change")
}