package ch.loewenfels.issuetrackingsync.app

import ch.loewenfels.issuetrackingsync.client.ClientFactory
import ch.loewenfels.issuetrackingsync.client.DefaultClientFactory
import ch.loewenfels.issuetrackingsync.settings.Settings
import com.fasterxml.jackson.databind.ObjectMapper
import com.ibm.team.repository.client.TeamPlatform
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.system.ApplicationHome
import org.springframework.context.annotation.Bean
import org.springframework.integration.config.EnableIntegrationManagement
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

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
open class IssueTrackingSyncApp : WebSecurityConfigurerAdapter() {
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

    @PostConstruct
    fun onStartup() {
        TeamPlatform.startup()
    }

    @PreDestroy
    fun onShutdown() {
        TeamPlatform.shutdown()
    }

    /**
     * Disable CSRF as this app will be run as an internal app only. Should security ever be a concern,
     * enable CSRF, and adapt index.html accordingly
     */
    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
    }
}

fun main(args: Array<String>) {
    runApplication<IssueTrackingSyncApp>(*args)
}
