package ch.loewenfels.issuetrackingsync.scheduling

import ch.loewenfels.issuetrackingsync.INTERNAL_QUEUE_NAME
import ch.loewenfels.issuetrackingsync.Issue
import ch.loewenfels.issuetrackingsync.Logging
import ch.loewenfels.issuetrackingsync.logger
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component

@Component
class SyncRequestProducer(private val jmsTemplate: JmsTemplate) : Logging {
    fun queue(issue: Issue) {
        logger().debug("Queueing {}", issue)

        jmsTemplate.send(INTERNAL_QUEUE_NAME) { session ->
            session.createObjectMessage(
                SyncRequest(
                    issue.key,
                    issue.clientSourceName,
                    issue.lastUpdated
                )
            )
        }
    }
}