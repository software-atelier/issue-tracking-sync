package ch.loewenfels.issuetrackingsync.syncclient

import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication

interface ClientFactory {
    fun getClient(clientSettings: IssueTrackingApplication): IssueTrackingClient
}

object DefaultClientFactory : ClientFactory {
    override fun getClient(clientSettings: IssueTrackingApplication): IssueTrackingClient {
        try {
            val clientClass = Class.forName(clientSettings.className) as Class<IssueTrackingClient>
            return clientClass.getConstructor(IssueTrackingApplication::class.java).newInstance(clientSettings)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to load client class " + clientSettings.className, e)
        }
    }
}
