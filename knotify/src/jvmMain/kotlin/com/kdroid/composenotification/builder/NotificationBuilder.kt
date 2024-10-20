package com.kdroid.composenotification.builder

import com.kdroid.composenotification.NotificationProvider
import com.kdroid.composenotification.models.ButtonModel
import com.kdroid.composenotification.models.DismissalReason
import com.kdroid.composenotification.platform.linux.LinuxNotificationProvider
import com.kdroid.composenotification.platform.windows.provider.WindowsNotificationProvider
import com.kdroid.composenotification.utils.OsUtils
import java.util.concurrent.CountDownLatch


/**
 * Displays a system notification with optional configuration for application name,
 * icon, title, message, large image, and additional builder actions.
 *
 * @param appName The name of the application sending the notification.
 * @param appIconPath Optional path to the application's icon.
 * @param title The title of the notification.
 * @param message The message content of the notification.
 * @param largeImagePath Optional path to a large image to be included in the notification.
 * @param builderAction Lambda with receiver to configure additional properties and actions on the notification.
 */
fun Notification(
    appName: String = "NotificationExample",
    appIconPath: String? = null,
    title: String = "",
    message: String = "",
    largeImagePath: String? = null,
    builderAction: NotificationBuilder.() -> Unit = {}
) {
    val builder = NotificationBuilder(appName, appIconPath, title, message, largeImagePath)
    builder.builderAction()
    builder.send()

    // Attendre que la notification soit traitée
    builder.waitForCompletion()
}

class NotificationBuilder(
    var appName: String,
    var appIconPath: String?,
    var title: String = "",
    var message: String = "",
    var largeImagePath: String?
) {
    internal val buttons = mutableListOf<ButtonModel>()

    internal var onActivated: (() -> Unit)? = null
    internal var onDismissed: ((DismissalReason) -> Unit)? = null
    internal var onFailed: (() -> Unit)? = null

    // Latch pour synchroniser l'attente de la notification
    private val latch = CountDownLatch(1)

    fun Button(label: String, onClick: () -> Unit) {
        buttons.add(ButtonModel(label, onClick))
    }

    fun onActivated(callback: () -> Unit) {
        this.onActivated = callback
    }

    fun onDismissed(callback: (DismissalReason) -> Unit) {
        this.onDismissed = callback
    }

    fun onFailed(callback: () -> Unit) {
        this.onFailed = callback
    }

    fun send() {
        val notificationProvider: NotificationProvider = when {
            OsUtils.isLinux() -> LinuxNotificationProvider()
            OsUtils.isWindows() -> WindowsNotificationProvider()
            else -> throw UnsupportedOperationException("Unsupported OS")
        }

        // Passer le latch au fournisseur de notifications
        if (notificationProvider is WindowsNotificationProvider) {
            notificationProvider.setCompletionLatch(latch)
        }

        notificationProvider.sendNotification(this)

        // Pour les autres OS, vous pouvez décompter le latch immédiatement
        if (notificationProvider !is WindowsNotificationProvider) {
            latch.countDown()
        }
    }

    fun waitForCompletion() {
        latch.await()
    }
}
