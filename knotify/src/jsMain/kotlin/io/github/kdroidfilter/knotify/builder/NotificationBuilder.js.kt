package io.github.kdroidfilter.knotify.builder

import androidx.compose.runtime.State
import org.w3c.notifications.*
import org.w3c.notifications.Notification.Companion.permission

// Actual implementation for the JS platform
actual fun getNotificationProvider(): NotificationProvider = JsNotificationProvider()

class JsNotificationProvider : NotificationProvider {
    override val hasPermissionState: State<Boolean>
        get() = TODO("Not yet implemented")

    override fun sendNotification(builder: NotificationBuilder) {
        if (permission == NotificationPermission.GRANTED) {
            val options = NotificationOptions(
                body = builder.message,
                icon = builder.largeImagePath as? String,
                actions = builder.buttons.map { button ->
                    NotificationAction(
                        action = button.label,
                        title = button.label
                    )
                }.toTypedArray()
            )

            val notification = Notification(builder.title, options)

            // Handle notification click event
            notification.onclick = {
                builder.onActivated?.invoke()
                // Close the notification
                notification.close()
            }

            // Handle notification close event not available

            // Handle notification error event
            notification.onerror = {
                builder.onFailed?.invoke()
            }
        } else {
            // Permission not granted
            builder.onFailed?.invoke()
        }
    }

    override fun hasPermission(): Boolean {
        return permission == NotificationPermission.GRANTED
    }

}


