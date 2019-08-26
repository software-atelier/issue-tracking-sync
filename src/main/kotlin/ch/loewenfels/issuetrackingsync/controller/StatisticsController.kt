package ch.loewenfels.issuetrackingsync.controller

import ch.loewenfels.issuetrackingsync.INTERNAL_QUEUE_NAME
import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class StatisticsController constructor(
    val jmsTemplate: JmsTemplate,
    val activeMQConnectionFactory: ActiveMQConnectionFactory
) {
    @GetMapping("/statistics")
    fun statistics(): Map<String, String> {
        val result: MutableMap<String, String> = HashMap();

        jmsTemplate.browse(INTERNAL_QUEUE_NAME) { _, browser ->
            val messages = browser.enumeration
            var total = 0
            while (messages.hasMoreElements()) {
                messages.nextElement()
                total++
            }
            result.put("Total elements in queue", Integer.toString(total))
        }
        var jmsStats = activeMQConnectionFactory.stats
        jmsStats.statisticNames.forEach {
            val subStats = jmsStats.getStatistic(it)
            result[subStats.name] = subStats.description
        }

        result.put("hello", "there")
        // TODO: can we get access to these?
        //     private final JMSStatsImpl factoryStats;
        //    private final JMSConnectionStatsImpl stats;
        // TODO: check https://stackoverflow.com/questions/30183907/activemq-how-to-programmatically-monitor-embedded-broker
        // TODO: check https://stackoverflow.com/questions/57612384/how-to-access-jms-statistics-in-spring-boot
        return result;
    }
}
