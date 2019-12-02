package ch.loewenfels.issuetrackingsync.custom

internal class UnclosedDefectFilterTest : UnclosedFilterTest() {
    override fun getUnclosedFilter() = UnclosedDefectFilter()

    override fun getIssueTypePassingFilterRtc() = "ch.igs.team.apt.workItemType.defect"

    override fun getIssueTypeNotPassingFilterRtc() = "ch.igs.team.apt.workItemType.change"

    override fun getIssueTypeNotPassingFilterJira() = "IGS Change"

    override fun getIssueTypePassingFilterJira() = "IGS Defect"
}