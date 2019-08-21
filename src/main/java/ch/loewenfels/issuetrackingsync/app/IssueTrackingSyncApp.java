package ch.loewenfels.issuetrackingsync.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;

import ch.loewenfels.issuetrackingsync.settings.Settings;

import com.fasterxml.jackson.databind.ObjectMapper;

@EnableAutoConfiguration
public class IssueTrackingSyncApp {
    @Value("${sync.settingsLocation}")
    private String fileLocation;

    @Bean
    public Settings settings(@Autowired final ObjectMapper objectMapper) {
        return Settings.loadFromFile(fileLocation, objectMapper);
    }

    public static void main(final String[] args) {
        SpringApplication.run(IssueTrackingSyncApp.class, args);
    }
}
