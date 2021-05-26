package ch.loewenfels.issuetrackingsync.custom

import ch.loewenfels.issuetrackingsync.syncclient.IssueQueryBuilder
import com.ibm.team.workitem.common.expression.AttributeExpression
import com.ibm.team.workitem.common.expression.IQueryableAttribute
import com.ibm.team.workitem.common.expression.Term
import com.ibm.team.workitem.common.model.AttributeOperation

class RtcIssueQueryBuilder: IssueQueryBuilder {

    override fun build(field: Any, fieldValue: String): Any {
        val issuesTerm = Term(Term.Operator.OR)

        issuesTerm.add(AttributeExpression(
                field as IQueryableAttribute?,
                AttributeOperation.EQUALS,
                fieldValue
        ))

        issuesTerm.add(AttributeExpression(
                field as IQueryableAttribute?,
                AttributeOperation.EQUALS,
                ";$fieldValue;"
        ))

        issuesTerm.add(AttributeExpression(
                field as IQueryableAttribute?,
                AttributeOperation.STARTS_WITH,
                "$fieldValue;"
        ))

        issuesTerm.add(AttributeExpression(
                field as IQueryableAttribute?,
                AttributeOperation.ENDS_WITH,
                ";$fieldValue"
        ))

        return issuesTerm
    }
}