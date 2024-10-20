package com.kdroid.composenotification.builder

import com.kdroid.composenotification.NotificationProvider
import com.kdroid.composenotification.models.ButtonModel
import com.kdroid.composenotification.models.DismissalReason
import com.kdroid.composenotification.platform.linux.LinuxNotificationProvider
import com.kdroid.composenotification.platform.windows.provider.WindowsNotificationProvider
import com.kdroid.composenotification.utils.OsUtils


/**
 * Displays a notification with customizable settings. The notification can have an app name,
 * icon, title, message, and a large image. Additionally, various actions can be added to the
 * notification using a builder-style DSL.
 *
 * @param appName The name of the application displaying the notification. Defaults to "NotificationExample".
 * @param appIconPath The file path to the application's icon. Can be null.
 * @param title The title of the notification. Defaults to an empty string.
 * @param message The message of the notification. Defaults to an empty string.
 * @param largeImagePath The file path to a large image to be displayed within the notification. Can be null.
 * @param builderAction A DSL block that customizes the notification options and actions.
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

        notificationProvider.sendNotification(this)
    }
}
