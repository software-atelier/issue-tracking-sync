package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import com.ibm.team.foundation.common.text.XMLString

/**
 * This mapper passes around HTML values. Reading from JIRA, this means getting the HTML rendered
 * value when reading the value (RTC already delivers an [XMLString]). On write to JIRA, an [XMLString]
 * is converted to JIRA markup, while writing to RTC sends the HTML unaltered.
 */
open class HtmlFieldMapper : FieldMapper {
    override fun <T> getValue(
        proprietaryIssue: T,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>
    ): Any? = issueTrackingClient.getHtmlValue(proprietaryIssue, fieldname)

    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        val convertibleValue: String = if (value is XMLString) value.xmlText else value?.toString() ?: ""
        issueTrackingClient.setHtmlValue(proprietaryIssueBuilder, issue, fieldname, convertibleValue)
    }
}