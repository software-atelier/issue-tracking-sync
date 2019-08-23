package ch.loewenfels.issuetrackingsync

import ch.loewenfels.issuetrackingsync.app.IssueTrackingSyncApp
import ch.loewenfels.issuetrackingsync.client.ClientFactory
import ch.loewenfels.issuetrackingsync.settings.IssueTrackingApplication
import ch.loewenfels.issuetrackingsync.testcontext.MockJiraClient
import ch.loewenfels.issuetrackingsync.testcontext.MockRtcClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [IssueTrackingSyncApp::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class AbstractSpringTest {
    @MockBean
    lateinit var clientFactory: ClientFactory

    @BeforeEach
    fun trainClientFactory() {
//        doAnswer {
//            val setting = it.arguments[0] as IssueTrackingApplication
//            when {
//                setting.className.endsWith("JiraClient") -> MockJiraClient(setting)
//                setting.className.endsWith("RtcClient") -> MockRtcClient(setting)
//                else -> throw IllegalArgumentException("Unknown client: " + setting.className)
//            }
//        }.`when`(clientFactory).getClient(any(IssueTrackingApplication::class.java))
        `when`(clientFactory.getClient(any(IssueTrackingApplication::class.java))).thenAnswer {
            val setting = it.arguments[0] as IssueTrackingApplication
            when {
                setting.className.endsWith("JiraClient") -> MockJiraClient(setting)
                setting.className.endsWith("RtcClient") -> MockRtcClient(setting)
                else -> throw IllegalArgumentException("Unknown client: " + setting.className)
            }
        }
    }
}

/**
 * Kotlin's "not null" paradigm clashing with Mockito is well documented, see
 * eg. https://stackoverflow.com/questions/51868577/how-do-you-get-mockito-to-play-nice-with-kotlin-non-nullable-types
 */
private fun <T> any(type: Class<T>): T {
    Mockito.any(type)
    return null as T
}