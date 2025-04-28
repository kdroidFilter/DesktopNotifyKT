package io.github.kdroidfilter.knotify.builder

import io.github.kdroidfilter.knotify.model.Button
import io.github.kdroidfilter.knotify.model.DismissalReason


/**
 * Marks the notifications API as experimental and subject to change in future releases.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This notifications API is experimental and may change in the future."
)
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalNotificationsApi

/**
 * Creates a notification with customizable settings. The notification can have an app name,
 * icon, title, message, and a large image. Additionally, various actions can be added to the
 * notification using a builder-style DSL.
 *
 * @param title The title of the notification. Defaults to an empty string.
 * @param message The message of the notification. Defaults to an empty string.
 * @param largeImage The file path to a large image to be displayed within the notification. Can be null.
 * @param onActivated Callback that is invoked when the notification is activated.
 * @param onDismissed Callback that is invoked when the notification is dismissed.
 * @param onFailed Callback that is invoked when the notification fails to display.
 * @param builderAction A DSL block that customizes the notification options and actions.
 * @return A Notification object that can be sent or hidden.
 */
@ExperimentalNotificationsApi
fun notification(
    title: String = "",
    message: String = "",
    largeImage: String? = null,
    onActivated: (() -> Unit)? = null,
    onDismissed: ((DismissalReason) -> Unit)? = null,
    onFailed: (() -> Unit)? = null,
    builderAction: NotificationBuilder.() -> Unit = {}
): Notification {
    val builder = NotificationBuilder(title, message, largeImage, onActivated, onDismissed, onFailed)
    builder.builderAction()
    return Notification(builder)
}

/**
 * Creates and immediately sends a notification with customizable settings.
 *
 * @param title The title of the notification. Defaults to an empty string.
 * @param message The message of the notification. Defaults to an empty string.
 * @param largeImage The file path to a large image to be displayed within the notification. Can be null.
 * @param onActivated Callback that is invoked when the notification is activated.
 * @param onDismissed Callback that is invoked when the notification is dismissed.
 * @param onFailed Callback that is invoked when the notification fails to display.
 * @param builderAction A DSL block that customizes the notification options and actions.
 * @return A Notification object that can be hidden or manipulated later.
 */
@ExperimentalNotificationsApi
fun sendNotification(
    title: String = "",
    message: String = "",
    largeImage: String? = null,
    onActivated: (() -> Unit)? = null,
    onDismissed: ((DismissalReason) -> Unit)? = null,
    onFailed: (() -> Unit)? = null,
    builderAction: NotificationBuilder.() -> Unit = {}
): Notification {
    val notification = notification(title, message, largeImage, onActivated, onDismissed, onFailed, builderAction)
    notification.send()
    return notification
}

/**
 * Displays a notification with customizable settings (legacy method).
 * This function immediately sends the notification.
 *
 * @param title The title of the notification. Defaults to an empty string.
 * @param message The message of the notification. Defaults to an empty string.
 * @param largeImage The file path to a large image to be displayed within the notification. Can be null.
 * @param onActivated Callback that is invoked when the notification is activated.
 * @param onDismissed Callback that is invoked when the notification is dismissed.
 * @param onFailed Callback that is invoked when the notification fails to display.
 * @param builderAction A DSL block that customizes the notification options and actions.
 * @deprecated Use notification() to create and myNotification.send() to send, or sendNotification() to create and send immediately
 */
@ExperimentalNotificationsApi
@Deprecated("Use notification() to create and myNotification.send() to send, or sendNotification() to create and send immediately", ReplaceWith("sendNotification(title, message, largeImage, onActivated, onDismissed, onFailed, builderAction)"))
fun Notification(
    title: String = "",
    message: String = "",
    largeImage: String? = null,
    onActivated: (() -> Unit)? = null,
    onDismissed: ((DismissalReason) -> Unit)? = null,
    onFailed: (() -> Unit)? = null,
    builderAction: NotificationBuilder.() -> Unit = {}
) {
    sendNotification(title, message, largeImage, onActivated, onDismissed, onFailed, builderAction)
}

/**
 * A notification that can be sent or hidden.
 */
class Notification internal constructor(private val builder: NotificationBuilder) {
    /**
     * Sends the notification.
     */
    fun send() {
        val notificationProvider = getNotificationProvider()
        notificationProvider.sendNotification(builder)
    }

    /**
     * Hides the notification.
     */
    fun hide() {
        val notificationProvider = getNotificationProvider()
        notificationProvider.hideNotification(builder)
    }
}

class NotificationBuilder(
    var title: String = "",
    var message: String = "",
    var largeImagePath: String?,
    var onActivated: (() -> Unit)? = null,
    var onDismissed: ((DismissalReason) -> Unit)? = null,
    var onFailed: (() -> Unit)? = null,
) {
    internal val buttons = mutableListOf<Button>()

    /**
     * Adds a button to the notification.
     *
     * @param title The title of the button.
     * @param onClick Callback that is invoked when the button is clicked.
     */
    fun button(title: String, onClick: () -> Unit) {
        buttons.add(io.github.kdroidfilter.knotify.model.Button(title, onClick))
    }

    /**
     * Adds a button to the notification (legacy method).
     * 
     * @param label The label of the button.
     * @param onClick Callback that is invoked when the button is clicked.
     * @deprecated Use button(title, onClick) instead
     */
    @Deprecated("Use button(title, onClick) instead", ReplaceWith("button(label, onClick)"))
    fun Button(label: String, onClick: () -> Unit) {
        button(label, onClick)
    }
}

expect fun getNotificationProvider(): NotificationProvider
