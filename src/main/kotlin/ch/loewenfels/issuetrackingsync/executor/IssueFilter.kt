package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.Issue

interface IssueFilter {
    fun test(issue: Issue): Boolean
}