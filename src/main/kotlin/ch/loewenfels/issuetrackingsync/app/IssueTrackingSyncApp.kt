package ch.loewenfels.issuetrackingsync.app

import ch.loewenfels.issuetrackingsync.client.ClientFactory
import ch.loewenfels.issuetrackingsync.client.DefaultClientFactory
import ch.loewenfels.issuetrackingsync.settings.Settings
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.system.ApplicationHome
import org.springframework.context.annotation.Bean
import org.springframework.integration.config.EnableIntegrationManagement
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(
    scanBasePackages = ["ch.loewenfels.issuetrackingsync.controller",//
        "ch.loewenfels.issuetrackingsync.scheduled", //
        "ch.loewenfels.issuetrackingsync.executor"
    ]
)
@EnableScheduling
@EnableIntegrationManagement(
    defaultLoggingEnabled = "true",
    defaultCountsEnabled = "true",
    defaultStatsEnabled = "true"
)
open class IssueTrackingSyncApp {
    @Value("\${sync.settingsLocation}")
    lateinit var settingsLocation: String;

    @Bean
    open fun settings(@Autowired objectMapper: ObjectMapper): Settings {
        return Settings.loadFromFile(settingsLocation, objectMapper)
    }

    @Bean
    open fun appState(@Autowired objectMapper: ObjectMapper): AppState {
        val home: ApplicationHome = ApplicationHome(javaClass)
        return AppState.loadFromFile(home.dir, objectMapper)
    }

    @Bean
    open fun clientFactory(): ClientFactory {
        return DefaultClientFactory
    }
}

fun main(args: Array<String>) {
    runApplication<IssueTrackingSyncApp>(*args)
}
