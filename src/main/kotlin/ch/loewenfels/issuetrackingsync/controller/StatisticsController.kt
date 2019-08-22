package ch.loewenfels.issuetrackingsync.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class StatisticsController {
    @GetMapping("/statistics")
    fun statistics(): Map<String, String> {
        val result: MutableMap<String, String> = HashMap();
        result.put("hello", "there")
        return result;
    }
}
