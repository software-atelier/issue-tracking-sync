package ch.loewenfels.issuetrackingsync.scheduling

import ch.loewenfels.issuetrackingsync.INTERNAL_QUEUE_NAME
import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.Logging
import ch.loewenfels.issuetrackingsync.executor.SynchronizationFlowFactory
import ch.loewenfels.issuetrackingsync.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import javax.jms.Message
import javax.jms.ObjectMessage

@Component
class SyncRequestConsumer @Autowired constructor(
    private val synchronizationFlowFactory: SynchronizationFlowFactory
) : Logging {
    @JmsListener(destination = INTERNAL_QUEUE_NAME)
    fun onMessage(message: Message) {
        if (message is ObjectMessage) {
            val messageData: SyncRequest = message.`object` as SyncRequest
            processSyncRequest(toIssue(messageData))
        }
        message.acknowledge()
    }

    private fun toIssue(syncRequest: SyncRequest) =
        Issue(syncRequest.key, syncRequest.clientSourceName, syncRequest.lastUpdated)

    private fun processSyncRequest(issue: Issue) {
        logger().debug("Processing issue {}", issue)
        synchronizationFlowFactory.getSynchronizationFlow(issue.clientSourceName, issue)?.execute(issue)
    }
}