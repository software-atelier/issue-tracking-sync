package ch.loewenfels.issuetrackingsync.scheduling

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import ch.loewenfels.issuetrackingsync.Issue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

internal class SyncRequestProducerTest : AbstractSpringTest() {
    @Autowired
    lateinit var syncRequestProducer: SyncRequestProducer;

    @Test
    fun queue_validIssue_sentViaJmsTemplate() {
        // arrange
        val issue = Issue("MK-1", "JIRA", LocalDateTime.now())
        // act
        syncRequestProducer.queue(issue)
        // assert
        Mockito.verify(jmsTemplate).send(Mockito.anyString(), Mockito.any())
    }
}