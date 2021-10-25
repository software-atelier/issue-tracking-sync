package ch.loewenfels.issuetrackingsync.syncclient.file

import ch.loewenfels.issuetrackingsync.*
import ch.loewenfels.issuetrackingsync.executor.SyncActionName
import ch.loewenfels.issuetrackingsync.executor.actions.SynchronizationAction
import ch.loewenfels.issuetrackingsync.notification.NotificationObserver
import ch.loewenfels.issuetrackingsync.syncclient.IssueTrackingClient
import ch.loewenfels.issuetrackingsync.syncconfig.DefaultsForNewIssue
import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.FileWriter
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

class FileClient(private val setup: IssueTrackingApplication) :
  IssueTrackingClient<FileIssue>, Logging {

  private val yamlReader = ObjectMapper(YAMLFactory())

  init {
    yamlReader.findAndRegisterModules()
    val endpointPath = Path.of(setup.endpoint).toFile()
    if (!endpointPath.exists() && endpointPath.mkdir())
      logger().warn("FileClient folder created: $endpointPath")
  }

  override fun getIssue(key: String): Issue? {
    val issueData = getProprietaryIssue(key) ?: return null
    return Issue(issueData.key, setup.name, issueData.lastUpdated)
  }

  override fun getIssueFromWebhookBody(body: JsonNode): Issue {
    TODO("Not yet implemented")
  }

  override fun getProprietaryIssue(issue: Issue): FileIssue? {
    return getProprietaryIssue(issue.key)
  }

  override fun getProprietaryIssue(issueKey: String): FileIssue? {
    val issueFile = Path.of(setup.endpoint, "$issueKey.yml").toFile()
    return if (issueFile.exists()) {
      val issue = yamlReader.readValue(issueFile, FileIssue::class.java)
      issue.key = issueKey
      issue
    } else {
      logger().info("Issue $issueKey not found in path ${issueFile.absoluteFile}")
      null
    }
  }

  override fun getProprietaryIssue(fieldName: String, fieldValue: String): FileIssue? {
    TODO("Not yet implemented")
  }

  override fun searchProprietaryIssues(fieldName: String, fieldValue: String): List<FileIssue> {
    TODO("Not yet implemented")
  }

  override fun getLastUpdated(internalIssue: FileIssue): LocalDateTime {
    return internalIssue.lastUpdated
  }

  override fun getKey(internalIssue: FileIssue): String {
    return internalIssue.key
  }

  override fun getIssueUrl(internalIssue: FileIssue): String {
    return Paths.get(setup.endpoint, internalIssue.key + ".yml").toFile().absolutePath
  }

  override fun getValue(internalIssue: FileIssue, fieldName: String): Any? {
    return when (fieldName) {
      "summary" -> internalIssue.summary
      "status" -> internalIssue.status
      "description" -> internalIssue.description
      "lastUpdated" -> internalIssue.lastUpdated
      else -> internalIssue.attributes[fieldName]
    }
  }

  override fun setValue(internalIssueBuilder: Any, issue: Issue, fieldName: String, value: Any?) {
    val targetIssue = internalIssueBuilder as FileIssue
    when (fieldName) {
      "summary" -> targetIssue.summary = value as String
      "status" -> targetIssue.status = value as String
      "description" -> targetIssue.description = value as String
      "lastUpdated" -> targetIssue.lastUpdated = value as LocalDateTime
      else -> if (value == null)
        targetIssue.attributes.remove(fieldName)
      else targetIssue.attributes[fieldName] = value as String
    }
  }

  override fun getComments(internalIssue: FileIssue): List<Comment> {
    return internalIssue.comments
  }

  override fun addComment(internalIssue: FileIssue, comment: Comment) {
    TODO("Not yet implemented")
  }

  override fun getAttachments(internalIssue: FileIssue): List<Attachment> {
    return internalIssue.attachments
  }

  override fun addAttachment(internalIssue: FileIssue, attachment: Attachment) {
    TODO("Not yet implemented")
  }

  override fun getMultiSelectValues(internalIssue: FileIssue, fieldName: String): List<String> {
    TODO("Not yet implemented")
  }

  override fun getState(internalIssue: FileIssue): String {
    TODO("Not yet implemented")
  }

  override fun getStateHistory(internalIssue: FileIssue): List<StateHistory> {
    TODO("Not yet implemented")
  }

  override fun setState(internalIssue: FileIssue, targetState: String) {
    TODO("Not yet implemented")
  }

  override fun createOrUpdateTargetIssue(issue: Issue, defaultsForNewIssue: DefaultsForNewIssue?) {
    val targetIssue = (issue.proprietaryTargetInstance ?: getProprietaryIssue(issue)) as FileIssue?
    when {
      targetIssue != null -> updateTargetIssue(targetIssue, issue)
      defaultsForNewIssue != null -> createTargetIssue(defaultsForNewIssue, issue)
      else -> {
        val targetIssueKey = issue.keyFieldMapping!!.getKeyForTargetIssue().toString()
        throw SynchronizationAbortedException("No target issue found for $targetIssueKey, and no defaults for creating issue were provided")
      }
    }
  }

  private fun updateTargetIssue(targetIssue: FileIssue, issue: Issue) {
    issue.proprietaryTargetInstance = targetIssue
    issue.targetKey = getKey(targetIssue)
    issue.targetUrl = getIssueUrl(targetIssue)

    issue.fieldMappings.forEach {
      it.setTargetValue(targetIssue, issue, this)
    }

    if (targetIssue != getProprietaryIssue(targetIssue.key))
      yamlReader.writeValue(FileWriter(issue.targetUrl), targetIssue)
  }

  private fun createTargetIssue(
    defaultsForNewIssue: DefaultsForNewIssue,
    issue: Issue
  ): FileIssue {
    val fileIssue = FileIssue(
      summary = defaultsForNewIssue.additionalFields.simpleTextFields["summary"] ?: "",
      status = defaultsForNewIssue.additionalFields.simpleTextFields["status"] ?: "",
      description = defaultsForNewIssue.additionalFields.simpleTextFields["description"] ?: "",
      lastUpdated = LocalDateTime.now(),
    )
    fileIssue.let {
      it.key = issue.key
      it.description = defaultsForNewIssue.additionalFields.simpleTextFields["lastUpdatedBy"] ?: ""
    }
    updateTargetIssue(fileIssue, issue)
    return fileIssue
  }

  override fun changedIssuesSince(
    lastPollingTimestamp: LocalDateTime,
    batchSize: Int,
    offset: Int
  ): Collection<Issue> {
    TODO("Not yet implemented")
  }

  override fun getHtmlValue(internalIssue: FileIssue, fieldName: String): String? {
    TODO("Not yet implemented")
  }

  override fun prepareHtmlValue(htmlString: String): String {
    TODO("Not yet implemented")
  }

  override fun setHtmlValue(
    internalIssueBuilder: Any,
    issue: Issue,
    fieldName: String,
    htmlString: String
  ) {
    TODO("Not yet implemented")
  }

  override fun getTimeValueInMinutes(internalIssue: Any, fieldName: String): Number {
    TODO("Not yet implemented")
  }

  override fun setTimeValue(
    internalIssueBuilder: Any,
    issue: Issue,
    fieldName: String,
    timeInMinutes: Number?
  ) {
    TODO("Not yet implemented")
  }

  override fun logException(
    issue: Issue,
    exception: Exception,
    notificationObserver: NotificationObserver,
    syncActions: Map<SyncActionName, SynchronizationAction>
  ): Boolean {
    logger().debug(exception.message, exception)
    return true
  }

  override fun close() {
    TODO("Not yet implemented")
  }
}