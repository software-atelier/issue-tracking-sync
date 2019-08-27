package ch.loewenfels.issuetrackingsync.app

import ch.loewenfels.issuetrackingsync.notification.NotificationChannel
import org.springframework.boot.context.properties.ConfigurationProperties
import javax.annotation.PostConstruct

@ConfigurationProperties("sync")
open class SyncApplicationProperties {
    lateinit var settingsLocation: String;
    lateinit var pollingCron: String;
    var notificationChannelProperties: List<NotificationChannelProperties> = mutableListOf();
    var notificationChannels = ArrayList<NotificationChannel>()
        private set

    @PostConstruct
    fun loadChannels() {
        notificationChannelProperties.map {
            try {
                val channelClass = Class.forName(it.classname) as Class<NotificationChannel>
                channelClass.getConstructor(NotificationChannelProperties::class.java).newInstance(it)
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to load ${it} as notification channel class", e)
            }
        }
            .forEach { notificationChannels.add(it) }
    }
}

class NotificationChannelProperties {
    var classname: String = "";
    var endpoint: String = "";
    var username: String = "";
    var subject: String = "";
    var avatar: String = "";
}
