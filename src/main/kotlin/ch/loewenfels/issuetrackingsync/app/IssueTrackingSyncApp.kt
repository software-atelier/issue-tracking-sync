package ch.loewenfels.issuetrackingsync.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["ch.loewenfels.issuetrackingsync.controller"])
open class IssueTrackingSyncApp

fun main(args: Array<String>) {
    runApplication<IssueTrackingSyncApp>(*args)
}
