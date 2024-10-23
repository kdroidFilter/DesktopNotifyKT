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
    largeImagePath: Any? = null,
    onActivated: (() -> Unit)? = null,
    onDismissed: ((DismissalReason) -> Unit)? = null,
    onFailed: (() -> Unit)? = null,
    builderAction: NotificationBuilder.() -> Unit = {}
) {
    val builder = NotificationBuilder(title, message, largeImagePath, onActivated, onDismissed, onFailed)
    builder.builderAction()
    builder.send()
}

class NotificationBuilder(
    var title: String = "",
    var message: String = "",
    var largeImagePath: Any?,
    var onActivated: (() -> Unit)? = null,
    var onDismissed: ((DismissalReason) -> Unit)? = null,
    var onFailed: (() -> Unit)? = null,
) {
    internal val buttons = mutableListOf<Button>()


    fun Button(label: String, onClick: () -> Unit) {
        buttons.add(com.kdroid.composenotification.model.Button(label, onClick))
    }



    fun send() {
        val notificationProvider = getNotificationProvider()
        notificationProvider.sendNotification(this)
    }
}

expect fun getNotificationProvider(): NotificationProvider

/**
 * Checks if the application has permission to display notifications.
 *
 * @return True if the application has notification permission, false otherwise.
 */
fun hasNotificationPermission(): Boolean {
    val notificationProvider = getNotificationProvider()
    return notificationProvider.hasPermission()
}

/**
 * Requests permission to display notifications from the user.
 *
 * @param onGranted A callback that is invoked if the permission is granted.
 * @param onDenied A callback that is invoked if the permission is denied.
 */
fun requestNotificationPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
    val notificationProvider = getNotificationProvider()
    notificationProvider.requestPermission(onGranted, onDenied)
}
