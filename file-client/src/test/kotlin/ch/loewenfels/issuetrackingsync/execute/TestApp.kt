package ch.loewenfels.issuetrackingsync.execute

import ch.loewenfels.issuetrackingsync.Logging
import ch.loewenfels.issuetrackingsync.executor.SynchronizationFlowFactory
import ch.loewenfels.issuetrackingsync.logger
import ch.loewenfels.issuetrackingsync.notification.NotificationObserver
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncclient.file.FileClient
import ch.loewenfels.issuetrackingsync.syncclient.file.FileIssue
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import ch.loewenfels.issuetrackingsync.syncconfig.Settings
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path

class TestApp : Logging {
    private val yamlReader = ObjectMapper(YAMLFactory())

    init {
        yamlReader.findAndRegisterModules()
    }

    @Test
    fun execute_WhenNotExists_ThenCreate() {
        // arrange
        val expectedDir = Path.of("src/test/resources/FileIssues/expected").toFile()
        val fromDir = Path.of("src/test/resources/FileIssues/from").toFile()
        val toDir = Path.of("src/test/resources/FileIssues/to").toFile()
        val workingDir = Path.of("build/FileIssues/to").toFile()
        workingDir.deleteRecursively()
        toDir.copyRecursively(workingDir, overwrite = true)

        val settings = Settings.loadFromFile("src/test/resources/testApp.yml")
        val clientFactory = object : ClientFactory {
            override fun getClient(clientSettings: IssueTrackingApplication): IssueTrackingClient<Any> {
                return FileClient(clientSettings) as IssueTrackingClient<Any>
            }
        }
        val testee = SynchronizationFlowFactory(settings, clientFactory, NotificationObserver())
        testee.loadSyncFlows()

        val fromFileClient = clientFactory.getClient(settings.trackingApplications.get(0))
        fromDir.listFiles()!!.forEach {
            val issueKey = it.name.dropLast(4)
            logger().info("Synchronize $issueKey")
            val issue = fromFileClient.getIssue(issueKey)!!
            val synchronizationFlow = testee.getSynchronizationFlow(issue.clientSourceName, issue)!!
            // act
            synchronizationFlow.execute(issue)
            // assert
            val expected = yamlReader.readValue(File(expectedDir, "$issueKey.yml").inputStream(), FileIssue::class.java)
            val actual = yamlReader.readValue(File(workingDir, "$issueKey.yml").inputStream(), FileIssue::class.java)
            Assertions.assertEquals(expected, actual)
        }
    }

}

