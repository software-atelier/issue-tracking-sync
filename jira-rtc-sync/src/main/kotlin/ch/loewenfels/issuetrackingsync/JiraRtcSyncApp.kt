package ch.loewenfels.issuetrackingsync

import ch.loewenfels.issuetrackingsync.app.IssueTrackingSyncApp
import ch.loewenfels.issuetrackingsync.app.SyncApplicationProperties
import com.ibm.team.repository.client.TeamPlatform
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ImportResource
import org.springframework.scheduling.annotation.EnableScheduling
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@SpringBootApplication(
    scanBasePackages = ["ch.loewenfels.issuetrackingsync.controller",//
        "ch.loewenfels.issuetrackingsync.scheduling", //
        "ch.loewenfels.issuetrackingsync.executor"
    ]
)
@EnableScheduling
@EnableConfigurationProperties(SyncApplicationProperties::class)
@ImportResource("classpath:activemq.xml")
open class JiraRtcSyncApp : IssueTrackingSyncApp() {

    @PostConstruct
    fun onStartup() {
        TeamPlatform.startup()
    }

    @PreDestroy
    fun onShutdown() {
        TeamPlatform.shutdown()
    }
}

fun main(args: Array<String>) {
    runApplication<JiraRtcSyncApp>(*args)
}
