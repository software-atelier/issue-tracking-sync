package ch.loewenfels.issuetrackingsync.scheduling

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import javax.jms.TextMessage

internal class SyncRequestConsumerTest : AbstractSpringTest() {
    @Autowired
    lateinit var syncRequestConsumer: SyncRequestConsumer;

    @Test
    fun onMessage_validTextMessage_acknowledged() {
        // arrange
        val syncRequestAsJson =
            "{\"key\":\"MK-1\",\"clientSourceName\":\"JIRA\",\"lastUpdated\":\"2019-08-29T14:34:07.077\"}"
        val textMessage = Mockito.mock(TextMessage::class.java)
        Mockito.`when`(textMessage.text).thenReturn(syncRequestAsJson)
        // act
        syncRequestConsumer.onMessage(textMessage)
        // assert
        Mockito.verify(textMessage).acknowledge()
    }
}

