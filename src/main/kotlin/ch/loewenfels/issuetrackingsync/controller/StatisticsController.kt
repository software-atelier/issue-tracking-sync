package ch.loewenfels.issuetrackingsync.controller

import org.springframework.jms.core.JmsTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class StatisticsController constructor(val jmsTemplate: JmsTemplate) {
    @GetMapping("/statistics")
    fun statistics(): Map<String, String> {
        val result: MutableMap<String, String> = HashMap();
        val queue = "foobar"
        jmsTemplate.browse(queue, { session, browser ->
            val messages = browser.enumeration
            var total = 0
            while (messages.hasMoreElements()) {
                messages.nextElement()
                total++
            }
            System.out.println(String.format("Total %d elements waiting in %s", total, queue))
        })
        result.put("hello", "there")
        // TODO: can we get access to these?
        //     private final JMSStatsImpl factoryStats;
        //    private final JMSConnectionStatsImpl stats;
        // TODO: check https://stackoverflow.com/questions/30183907/activemq-how-to-programmatically-monitor-embedded-broker
        // TODO: check https://stackoverflow.com/questions/57612384/how-to-access-jms-statistics-in-spring-boot
        return result;
    }
}
