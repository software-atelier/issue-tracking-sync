package ch.loewenfels.issuetrackingsync.executor.actions

import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.Logging
import ch.loewenfels.issuetrackingsync.executor.fields.FieldMapping
import ch.loewenfels.issuetrackingsync.logger
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.AdditionalProperties
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue

class StateSynchronizationAction : AbstractSynchronizationAction(),
    SynchronizationAction, Logging {
    private lateinit var statesMapping: Map<String, String>

    override fun execute(
        sourceClient: IssueTrackingClient<Any>,
        targetClient: IssueTrackingClient<Any>,
        issue: Issue,
        fieldMappings: List<FieldMapping>,
        defaultsForNewIssue: DefaultsForNewIssue?
    ) = execute(sourceClient, targetClient, issue, fieldMappings, defaultsForNewIssue, AdditionalProperties())

    override fun execute(
        sourceClient: IssueTrackingClient<Any>,
        targetClient: IssueTrackingClient<Any>,
        issue: Issue,
        fieldMappings: List<FieldMapping>,
        defaultsForNewIssue: DefaultsForNewIssue?,
        additionalProperties: AdditionalProperties?
    ) {
        statesMapping = additionalProperties!!.statesMapping
        val internalSourceIssue = issue.proprietarySourceInstance
        val internalTargetIssue = issue.proprietaryTargetInstance
        if ((internalSourceIssue != null) && (internalTargetIssue != null)) {
            val sourceState = sourceClient.getState(internalSourceIssue)
            val targetState = targetClient.getState(internalTargetIssue)
            val additionalInformation = listOf(additionalProperties.allTransitions, additionalProperties.happyPath)
            if (sourceClient.getLastUpdated(internalSourceIssue) > targetClient.getLastUpdated(internalTargetIssue)) {
                targetClient.setState(internalTargetIssue, convertState(sourceState), additionalInformation)
            } else {
                sourceClient.setState(internalSourceIssue, convertState(targetState), additionalInformation)
            }
        } else {
            logger().warn("This action relies on a previous action loading source and target issues. Consider configuring a SimpleSynchronizationAction without any fieldMappings prior to this action")
        }
    }

    private fun convertState(state: String): String {
        return if (statesMapping.containsKey(state)) {
            statesMapping.getOrDefault(state, "")
        } else {
            val reversed = statesMapping.entries.associateBy({ it.value }) { it.key }
            reversed.getOrDefault(state, "")
        }
    }
}