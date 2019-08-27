package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.IssueFilter

class NotClosedFilter : IssueFilter {
    override fun test(issue: Issue): Boolean {
        return true
    }
}