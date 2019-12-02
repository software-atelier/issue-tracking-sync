package ch.loewenfels.issuetrackingsync.custom

internal class UnclosedChangeFilterTest : UnclosedFilterTest() {
    override fun getUnclosedFilter() = UnclosedChangeFilter()
    override fun getIssueTypePassingFilterRtc() = "ch.igs.team.apt.workItemType.change"

    override fun getIssueTypeNotPassingFilterRtc() = "ch.igs.team.apt.workItemType.defect"

    override fun getIssueTypeNotPassingFilterJira() = "IGS Defect"

    override fun getIssueTypePassingFilterJira() = "IGS Change"
}