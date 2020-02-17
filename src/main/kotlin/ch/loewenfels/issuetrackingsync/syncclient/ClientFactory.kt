package ch.loewenfels.issuetrackingsync.syncclient

import ch.loewenfels.issuetrackingsync.syncconfig.IssueTrackingApplication

interface ClientFactory {
    fun getClient(clientSettings: IssueTrackingApplication): IssueTrackingClient<Any>
}

object DefaultClientFactory : ClientFactory {
    @Suppress("UNCHECKED_CAST", "TooGenericExceptionCaught")
    override fun getClient(clientSettings: IssueTrackingApplication): IssueTrackingClient<Any> {
        try {
            val clientClass = Class.forName(clientSettings.className) as Class<IssueTrackingClient<Any>>
            return clientClass.getConstructor(IssueTrackingApplication::class.java).newInstance(clientSettings)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to load client class " + clientSettings.className, e)
        }
    }
}
