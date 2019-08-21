package ch.loewenfels.issuetrackingsync.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatisticsController {
    @GetMapping("/statistics")
    public Map<String, Serializable> getStatistics() {
        final Map<String, Serializable> result = new HashMap<>();
        return result;
    }
}
