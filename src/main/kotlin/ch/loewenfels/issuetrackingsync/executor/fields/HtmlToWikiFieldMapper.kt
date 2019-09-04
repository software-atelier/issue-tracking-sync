package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncclient.jira.JiraClient
import com.atlassian.renderer.wysiwyg.converter.DefaultWysiwygConverter
import com.ibm.team.foundation.common.text.XMLString

/**
 * This mapper passes around HTML values. Reading from JIRA, this means getting the HTML rendered
 * value when reading the value (RTC already delivers an [XMLString]). On write to JIRA, an [XMLString]
 * is converted to JIRA markup, while writing to RTC sends the HTML unaltered.
 */
open class HtmlToWikiFieldMapper : FieldMapper {
    override fun <T> getValue(
        proprietaryIssue: T,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>
    ): Any? {
        return if (issueTrackingClient is JiraClient) {
            issueTrackingClient.getHtmlValue(
                proprietaryIssue as com.atlassian.jira.rest.client.api.domain.Issue,
                fieldname
            )
        } else {
            issueTrackingClient.getValue(proprietaryIssue, fieldname)
        }
    }

    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issue: Issue,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        val convertedValue = if (value is XMLString) {
            DefaultWysiwygConverter().convertXHtmlToWikiMarkup(value.xmlText)
        } else {
            value?.toString() ?: ""
        }
        issueTrackingClient.setValue(proprietaryIssueBuilder, issue, fieldname, convertedValue)
    }
}