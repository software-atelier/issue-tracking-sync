package ch.loewenfels.issuetrackingsync.client

import ch.loewenfels.issuetrackingsync.settings.IssueTrackingApplication

object ClientFactory {
    fun getClient(clientSettings: IssueTrackingApplication): IssueTrackingClient {
        try {
            val clientClass = Class.forName(clientSettings.className) as Class<out IssueTrackingClient>
            return clientClass.getConstructor(IssueTrackingApplication::class.java).newInstance(clientSettings)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to load client class " + clientSettings.className, e)
        }
    }
}
