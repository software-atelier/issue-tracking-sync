package ch.loewenfels.issuetrackingsync.executor

import ch.loewenfels.issuetrackingsync.INTERNAL_QUEUE_NAME
import ch.loewenfels.issuetrackingsync.Logging
import ch.loewenfels.issuetrackingsync.dto.SyncRequest
import ch.loewenfels.issuetrackingsync.logger
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import javax.jms.Message
import javax.jms.ObjectMessage

@Component
class SyncRequestConsumer : Logging {
    @JmsListener(destination = INTERNAL_QUEUE_NAME)
    fun onMessage(message: Message) {
        if (message is ObjectMessage) {
            val messageData: SyncRequest = message.`object` as SyncRequest
            logger().debug("Processing sync request {}", messageData)
        }
        // BEWARE: The default AUTO_ACKNOWLEDGE mode does not provide proper reliability guarantees
    }
}