package com.kdroid.composenotification.builder

import com.kdroid.composenotification.model.Button
import com.kdroid.composenotification.model.DismissalReason

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
    title: String = "",
    message: String = "",
    largeImagePath: String? = null,
    builderAction: NotificationBuilder.() -> Unit = {}
) {
    val builder = NotificationBuilder(title, message, largeImagePath)
    builder.builderAction()
    builder.send()
}

class NotificationBuilder(
    var title: String = "",
    var message: String = "",
    var largeImagePath: String?
) {
    internal val buttons = mutableListOf<Button>()

    internal var onActivated: (() -> Unit)? = null
    internal var onDismissed: ((DismissalReason) -> Unit)? = null
    internal var onFailed: (() -> Unit)? = null

    fun Button(label: String, onClick: () -> Unit) {
        buttons.add(com.kdroid.composenotification.model.Button(label, onClick))
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
        val notificationProvider = getNotificationProvider()
        notificationProvider.sendNotification(this)
    }
}

expect fun getNotificationProvider(context: Any? = null): NotificationProvider

/**
 * Checks if the application has permission to display notifications.
 *
 * @return True if the application has notification permission, false otherwise.
 */
fun hasNotificationPermission(context: Any? = null): Boolean {
    val notificationProvider = getNotificationProvider(context)
    return notificationProvider.hasPermission()
}

/**
 * Requests permission to display notifications from the user.
 *
 * @param onGranted A callback that is invoked if the permission is granted.
 * @param onDenied A callback that is invoked if the permission is denied.
 */
fun requestNotificationPermission(context: Any? = null, onGranted: () -> Unit, onDenied: () -> Unit) {
    val notificationProvider = getNotificationProvider(context)
    notificationProvider.requestPermission(onGranted, onDenied)
}
