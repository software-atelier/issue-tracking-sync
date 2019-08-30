package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import com.atlassian.renderer.wysiwyg.converter.DefaultWysiwygConverter
import com.ibm.team.foundation.common.text.XMLString

class HtmlToWikiFieldMapper : FieldMapper {
    override fun <T> getValue(
        proprietaryIssue: T,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>
    ): Any? {
        return issueTrackingClient.getValue(proprietaryIssue, fieldname)
    }

    override fun <T> setValue(
        proprietaryIssueBuilder: Any,
        fieldname: String,
        issueTrackingClient: IssueTrackingClient<in T>,
        value: Any?
    ) {
        val wikiMarkup = if (value is XMLString) {
            DefaultWysiwygConverter().convertXHtmlToWikiMarkup(value.xmlText)
        } else {
            value
        }
        issueTrackingClient.setValue(proprietaryIssueBuilder, fieldname, wikiMarkup)
    }
}