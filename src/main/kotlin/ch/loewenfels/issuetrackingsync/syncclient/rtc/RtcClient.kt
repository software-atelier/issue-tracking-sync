package ch.loewenfels.issuetrackingsync.syncclient.rtc

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.ApplicationRole
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import com.ibm.team.process.client.IProcessClientService
import com.ibm.team.process.common.IProjectArea
import com.ibm.team.repository.client.ITeamRepository
import com.ibm.team.repository.client.TeamPlatform
import com.ibm.team.repository.common.TeamRepositoryException
import com.ibm.team.workitem.client.IWorkItemClient
import com.ibm.team.workitem.common.IAuditableCommon
import com.ibm.team.workitem.common.expression.AttributeExpression
import com.ibm.team.workitem.common.expression.IQueryableAttribute
import com.ibm.team.workitem.common.expression.QueryableAttributes
import com.ibm.team.workitem.common.expression.Term
import com.ibm.team.workitem.common.model.AttributeOperation
import com.ibm.team.workitem.common.model.IWorkItem
import com.ibm.team.workitem.common.query.IQueryResult
import com.ibm.team.workitem.common.query.IResolvedResult
import org.eclipse.core.runtime.NullProgressMonitor
import java.net.URI
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class RtcClient(private val setup: IssueTrackingApplication) : IssueTrackingClient {
    private val progressMonitor = NullProgressMonitor()
    private val teamRepository: ITeamRepository
    private val workItemClient: IWorkItemClient;

    init {
        teamRepository = TeamPlatform.getTeamRepositoryService().getTeamRepository(setup.endpoint)
        teamRepository.registerLoginHandler(LoginHandler())
        teamRepository.login(NullProgressMonitor())
        workItemClient = teamRepository.getClientLibrary(IWorkItemClient::class.java) as IWorkItemClient
    }

    override fun getIssue(key: String): Issue? {
        val workItem: IWorkItem? =
            workItemClient.findWorkItemById(Integer.parseInt(key), IWorkItem.SMALL_PROFILE, progressMonitor)
        return workItem?.let {
            toSyncIssue(it)
        }
    }

    override fun changedIssuesSince(lastPollingTimestamp: LocalDateTime): Collection<Issue> {
        val queryClient = workItemClient.queryClient
        val projectArea = getProjectArea()
        val searchTerms = buildSearchTermForChangedIssues(lastPollingTimestamp, projectArea)
        val resolvedResultOfWorkItems =
            queryClient.getResolvedExpressionResults(projectArea, searchTerms, IWorkItem.FULL_PROFILE)
        return toWorkItems(resolvedResultOfWorkItems).map { toSyncIssue(it) }
    }

    private fun buildSearchTermForChangedIssues(lastPollingTimestamp: LocalDateTime, projectArea: IProjectArea): Term {
        val projectAreaExpression = AttributeExpression(
            getQueryableAttribute(IWorkItem.PROJECT_AREA_PROPERTY, projectArea),
            AttributeOperation.EQUALS,
            projectArea
        )
        val relevantIssuesTerm = Term(Term.Operator.OR)
        relevantIssuesTerm.add(modifiedIssuesWithApplicationLink(lastPollingTimestamp, projectArea))
        if (setup.role != ApplicationRole.SLAVE) {
            val createdRecently =
                AttributeExpression(
                    getQueryableAttribute(IWorkItem.CREATION_DATE_PROPERTY, projectArea),
                    AttributeOperation.GREATER_OR_EQUALS,
                    Timestamp.valueOf(lastPollingTimestamp)
                )
            relevantIssuesTerm.add(createdRecently)
        }
        val searchTerm = Term(Term.Operator.AND)
        searchTerm.add(projectAreaExpression)
        searchTerm.add(relevantIssuesTerm)
        return searchTerm
    }

    private fun modifiedIssuesWithApplicationLink(
        lastPollingTimestamp: LocalDateTime,
        projectArea: IProjectArea
    ): Term {
        val partnerApplicationLinkNotEmptyExpressions =
            setup.fieldsHoldingPartnerApplicationKey.map {
                AttributeExpression(
                    getQueryableAttribute(it.value, projectArea),
                    AttributeOperation.NOT_EQUALS,
                    ""
                )
            }
        val modifiedRecently =
            AttributeExpression(
                getQueryableAttribute(IWorkItem.MODIFIED_PROPERTY, projectArea),
                AttributeOperation.GREATER_OR_EQUALS,
                Timestamp.valueOf(lastPollingTimestamp)
            )
        val modifiedIssuesTerm = Term(Term.Operator.AND)
        modifiedIssuesTerm.add(modifiedRecently)
        if (partnerApplicationLinkNotEmptyExpressions.isNotEmpty()) {
            val anyPartnerLinkTerm = Term(Term.Operator.OR)
            partnerApplicationLinkNotEmptyExpressions.forEach { anyPartnerLinkTerm.add(it) }
            modifiedIssuesTerm.add(anyPartnerLinkTerm)
        }
        return modifiedIssuesTerm
    }

    private fun getQueryableAttribute(attributeName: String, projectArea: IProjectArea): IQueryableAttribute {
        val auditableCommon: IAuditableCommon =
            teamRepository.getClientLibrary(IAuditableCommon::class.java) as IAuditableCommon
        return QueryableAttributes.getFactory(IWorkItem.ITEM_TYPE).findAttribute(
            projectArea,
            attributeName,
            auditableCommon,
            null
        )
    }

    @Throws(TeamRepositoryException::class)
    private fun getProjectArea(): IProjectArea {
        val processClient = teamRepository.getClientLibrary(IProcessClientService::class.java) as IProcessClientService
        val uri = URI.create(setup.rtcProjectArea?.replace(" ", "%20"))
        return processClient.findProcessArea(uri, null, null) as IProjectArea?
            ?: throw IllegalStateException("Project area ${setup.rtcProjectArea} not found.")
    }

    @Throws(TeamRepositoryException::class)
    private fun toWorkItems(resolvedResults: IQueryResult<IResolvedResult<IWorkItem>>): List<IWorkItem> {
        val result = LinkedList<IWorkItem>()
        while (resolvedResults.hasNext(progressMonitor)) {
            result.add(resolvedResults.next(progressMonitor).getItem())
        }
        return result
    }

    private fun toSyncIssue(workItem: IWorkItem): Issue {
        return Issue(
            Integer.toString(workItem.id),
            setup.name,
            LocalDateTime.ofInstant(workItem.modified().toInstant(), ZoneId.systemDefault())
        )
    }

    inner class LoginHandler : ITeamRepository.ILoginHandler, ITeamRepository.ILoginHandler.ILoginInfo {
        override fun getUserId(): String {
            return setup.username
        }

        override fun getPassword(): String {
            return setup.password
        }

        override fun challenge(repository: ITeamRepository?): ITeamRepository.ILoginHandler.ILoginInfo {
            return this
        }
    }
}
