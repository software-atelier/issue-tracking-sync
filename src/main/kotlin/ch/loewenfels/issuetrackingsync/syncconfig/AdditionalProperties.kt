package ch.loewenfels.issuetrackingsync.syncconfig

class AdditionalProperties(
    var preComment: String = "",
    var postComment: String = "",
    var allTransitions: Map<String, List<String>> = mutableMapOf(),
    var statesMapping: Map<String, String> = mutableMapOf(),
    var happyPath: List<String> = mutableListOf()
)
