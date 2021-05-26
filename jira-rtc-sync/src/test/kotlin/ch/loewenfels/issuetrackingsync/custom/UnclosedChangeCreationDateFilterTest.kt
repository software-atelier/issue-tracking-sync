package ch.loewenfels.issuetrackingsync.custom

internal class UnclosedChangeCreationDateFilterTest : UnclosedIssueCreationDateFilterTest() {
    override fun getUnclosedFilter() = UnclosedChangeCreationDateFilter()
    override fun getIssueTypePassingFilterRtc() = "ch.igs.team.apt.workItemType.change"

    override fun getIssueTypeNotPassingFilterRtc() = "ch.igs.team.apt.workItemType.defect"

    override fun getIssueTypeNotPassingFilterJira() = "IGS Defect"

    override fun getIssueTypePassingFilterJira() = "IGS Change"
}