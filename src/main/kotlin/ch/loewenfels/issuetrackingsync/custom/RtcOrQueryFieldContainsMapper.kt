package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.executor.fields.FieldMapper
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import com.ibm.team.workitem.common.expression.AttributeExpression
import com.ibm.team.workitem.common.expression.Expression
import com.ibm.team.workitem.common.expression.IQueryableAttribute
import com.ibm.team.workitem.common.expression.Term
import com.ibm.team.workitem.common.model.AttributeOperation

class RtcOrQueryFieldContainsMapper(fieldMappingDefinition: FieldMappingDefinition) : FieldMapper {
    val separator = if (null != fieldMappingDefinition.associations["separator"]) fieldMappingDefinition.associations["separator"] else ";"

    override fun <T> getValue(
            proprietaryIssue: T,
            fieldname: String,
            issueTrackingClient: IssueTrackingClient<in T>
    ): Any? {
        val value = issueTrackingClient.getValue(proprietaryIssue, fieldname)

        val result: ((attribute: IQueryableAttribute) -> Expression) = {
            val term = Term(Term.Operator.OR)
            term.add(AttributeExpression(
                    it,
                    AttributeOperation.EQUALS,
                    value
            ));

            term.add(AttributeExpression(
                    it,
                    AttributeOperation.EQUALS,
                    "$separator$value$separator"
            ));
            term.add(AttributeExpression(
                    it,
                    AttributeOperation.STARTS_WITH,
                    "$value$separator"
            ));
            term.add(AttributeExpression(
                    it,
                    AttributeOperation.ENDS_WITH,
                    "$separator$value"
            ));

            term
        }

        return result
    }

    override fun <T> setValue(
            proprietaryIssueBuilder: Any,
            fieldname: String,
            issue: Issue,
            issueTrackingClient: IssueTrackingClient<in T>,
            value: Any?
    ) {
        TODO("Not yet implemented")
    }
}