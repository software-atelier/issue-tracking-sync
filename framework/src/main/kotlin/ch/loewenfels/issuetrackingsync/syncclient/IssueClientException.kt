package ch.loewenfels.issuetrackingsync.syncclient

class IssueClientException : RuntimeException {
  constructor(message: String, ex: Exception?) : super(message, ex)
  constructor(message: String) : super(message)
  constructor(ex: Exception) : super(ex)
}