package ch.loewenfels.issuetrackingsync

import ch.loewenfels.issuetrackingsync.app.IssueTrackingSyncApp
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [IssueTrackingSyncApp::class])
@AutoConfigureMockMvc
abstract class AbstractSpringTest