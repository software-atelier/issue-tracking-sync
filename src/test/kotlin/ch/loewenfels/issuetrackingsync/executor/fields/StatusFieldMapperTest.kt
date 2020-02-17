package ch.loewenfels.issuetrackingsync.executor.fields

import ch.loewenfels.issuetrackingsync.*
import ch.loewenfels.issuetrackingsync.syncclient.ClientFactory
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.FieldMappingDefinition
import ch.loewenfels.issuetrackingsync.testcontext.TestObjects
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

class StatusFieldMapperTest : AbstractSpringTest() {
    @Autowired
    private lateinit var clientFactory: ClientFactory
    private val jiraFieldname = "statusFieldJira"
    private val rtcFieldname = "statusFieldRtc"
    private lateinit var sourceClient: IssueTrackingClient<Any>
    private lateinit var targetClient: IssueTrackingClient<Any>
    private lateinit var sourceIssue: Issue
    private lateinit var targetIssue: Issue

    @BeforeEach
    fun initClients() {
        this.sourceClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("RtcClient"), clientFactory)
        this.targetClient =
            TestObjects.buildIssueTrackingClient(TestObjects.buildIssueTrackingApplication("JiraClient"), clientFactory)
        this.sourceIssue = sourceClient.getIssue("1234") ?: throw IllegalArgumentException("Unknown key")
        this.targetIssue = targetClient.getIssue("MK-1") ?: throw IllegalArgumentException("Unknown key")
        sourceIssue.proprietarySourceInstance = this.sourceIssue
        sourceIssue.proprietaryTargetInstance = this.targetIssue
    }

    @Test
    fun getValue() {
        // arrange
        val testee = buildTestee()
        val issue = TestObjects.buildIssue("MK-1")
        // act
        val result = testee.getValue(issue, rtcFieldname, sourceClient)
        // assert
        assertNotNull(result)
        Assertions.assertTrue(result is Pair<*, *>)
        assertEquals("In Umsetzung", (result as Pair<*, *>).first)
        Assertions.assertTrue(result.second is List<*>)
    }

    @Test
    fun setValue_targetIssueAlreadyInSameState_noOp() {
        // arrange
        Mockito.`when`(targetClient.getState(safeEq(targetIssue))).thenReturn("In Work")
        val targetValue = Pair(
            "In Umsetzung", listOf(
                StateHistory(LocalDateTime.now().minusHours(5), "Neu", "In Abklärung"),
                StateHistory(LocalDateTime.now().minusHours(3), "In Abklärung", "Bereit zur Umsetzung"),
                StateHistory(LocalDateTime.now(), "Bereit zur Umsetzung", "In Umsetzung")
            )
        )
        val testee = buildTestee()
        // act
        testee.setValue(targetIssue, jiraFieldname, sourceIssue, targetClient, targetValue)
        // assert
        Mockito.verify(targetClient, Mockito.never()).setState(safeEq(targetIssue), Mockito.anyString())
    }

    @Test
    fun setValue_targetIssueStateBehind_updatedState() {
        // arrange
        Mockito.`when`(targetClient.getState(safeEq(targetIssue))).thenReturn("Open")
        val targetValue = Pair(
            "In Umsetzung", listOf(
                StateHistory(LocalDateTime.now().minusHours(5), "Neu", "In Abklärung"),
                StateHistory(LocalDateTime.now().minusHours(3), "In Abklärung", "Bereit zur Umsetzung"),
                StateHistory(LocalDateTime.now(), "Bereit zur Umsetzung", "In Umsetzung")
            )
        )
        val testee = buildTestee()
        // act
        testee.setValue(targetIssue, jiraFieldname, sourceIssue, targetClient, targetValue)
        // assert
        Mockito.verify(targetClient).setState(targetIssue, "Authorized")
        Mockito.verify(targetClient).setState(targetIssue, "In Work")
    }

    @Test
    fun setValue_sourceIssueHasStateJumps_updatedState() {
        // arrange
        Mockito.`when`(targetClient.getState(safeEq(targetIssue))).thenReturn("In Work")
        val targetValue = Pair(
            "In Abnahme", listOf(
                StateHistory(LocalDateTime.now().minusHours(5), "Neu", "In Abklärung"),
                StateHistory(LocalDateTime.now().minusHours(4), "In Abklärung", "Bereit zur Umsetzung"),
                StateHistory(LocalDateTime.now().minusHours(3), "Bereit zur Umsetzung", "In Umsetzung"),
                StateHistory(LocalDateTime.now().minusHours(2), "In Umsetzung", "Bereit zur Abnahme"),
                StateHistory(LocalDateTime.now(), "Bereit zur Abnahme", "In Abnahme")
            )
        )
        val testee = buildTestee()
        // act
        testee.setValue(targetIssue, jiraFieldname, sourceIssue, targetClient, targetValue)
        // assert
        Mockito.verify(targetClient).setState(targetIssue, "In Test")
        Mockito.verify(targetClient).setState(targetIssue, "Resolved")
    }

    @Test
    fun setValue_sourceIssueIsWithinStateJump_updatedState() {
        // arrange
        Mockito.`when`(targetClient.getState(safeEq(targetIssue))).thenReturn("In Test")
        val targetValue = Pair(
            "In Abnahme", listOf(
                StateHistory(LocalDateTime.now().minusHours(5), "Neu", "In Abklärung"),
                StateHistory(LocalDateTime.now().minusHours(4), "In Abklärung", "Bereit zur Umsetzung"),
                StateHistory(LocalDateTime.now().minusHours(3), "Bereit zur Umsetzung", "In Umsetzung"),
                StateHistory(LocalDateTime.now().minusHours(2), "In Umsetzung", "Bereit zur Abnahme"),
                StateHistory(LocalDateTime.now(), "Bereit zur Abnahme", "In Abnahme")
            )
        )
        val testee = buildTestee()
        // act
        testee.setValue(targetIssue, jiraFieldname, sourceIssue, targetClient, targetValue)
        // assert
        Mockito.verify(targetClient).setState(targetIssue, "Resolved")
    }

    private fun buildTestee(): StatusFieldMapper {
        val associations =
            mutableMapOf(
                "Neu" to "Open",
                "In Abklärung" to "Open",
                "Abklärung angehalten" to "Open",
                "Bereit zur Umsetzung" to "Authorized",
                "In Umsetzung" to "In Work",
                "In Umsetzung angehalten" to "Interrupted",
                "Bereit zur Abnahme" to "In Test,Resolved",
                "In Abnahme" to "Resolved",
                "Abnahme angehalten" to "Resolved",
                "Abgeschlossen" to "Closed"
            )
        val fieldDefinition = FieldMappingDefinition(
            rtcFieldname, jiraFieldname,
            StatusFieldMapper::class.toString(), associations
        )
        return StatusFieldMapper(fieldDefinition)
    }
}

