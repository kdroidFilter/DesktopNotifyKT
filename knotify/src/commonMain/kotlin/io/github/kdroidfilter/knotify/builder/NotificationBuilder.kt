package io.github.kdroidfilter.knotify.builder

import io.github.kdroidfilter.knotify.model.Button
import io.github.kdroidfilter.knotify.model.DismissalReason
import io.github.kdroidfilter.knotify.model.TextInputAction


/**
 * Marks the notifications API as experimental and subject to change in future releases.
 */
@Suppress("ExperimentalAnnotationRetention")
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
 * @param largeIcon The file path to a large image to be displayed within the notification. Can be null.
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
    largeIcon: String? = null,
    smallIcon: String? = null,
    onActivated: (() -> Unit)? = null,
    onDismissed: ((DismissalReason) -> Unit)? = null,
    onFailed: (() -> Unit)? = null,
    builderAction: NotificationBuilder.() -> Unit = {}
): Notification {
    val builder = NotificationBuilder(title, message, largeIcon, smallIcon, onActivated, onDismissed, onFailed)
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
suspend fun sendNotification(
    title: String = "",
    message: String = "",
    largeImage: String? = null,
    smallIcon: String? = null,
    onActivated: (() -> Unit)? = null,
    onDismissed: ((DismissalReason) -> Unit)? = null,
    onFailed: (() -> Unit)? = null,
    builderAction: NotificationBuilder.() -> Unit = {}
): Notification {
    val notification = notification(title, message, largeImage, smallIcon, onActivated, onDismissed, onFailed, builderAction)
    notification.send()
    return notification
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
    var smallIconPath: String? = null,
    var onActivated: (() -> Unit)? = null,
    var onDismissed: ((DismissalReason) -> Unit)? = null,
    var onFailed: (() -> Unit)? = null,
) {
    internal val buttons = mutableListOf<Button>()
    internal val textInputActions = mutableListOf<TextInputAction>()
    internal val id: Int = generateUniqueId()

    companion object {
        private var lastId = 0

        private fun generateUniqueId(): Int {
            return ++lastId
        }
    }

    /**
     * Adds a button to the notification.
     *
     * @param title The title of the button.
     * @param onClick Callback that is invoked when the button is clicked.
     */
    fun button(title: String, onClick: () -> Unit) {
        buttons.add(Button(title, onClick))
    }

    /**
     * Adds a text input action to the notification.
     *
     * @param id The unique identifier for this text input action
     * @param label The text displayed on the text input button
     * @param placeholder The placeholder text displayed in the text input field
     * @param onTextSubmitted Callback that is invoked when text is submitted, with the submitted text as parameter
     */
    @ExperimentalNotificationsApi
    fun textInput(id: String, label: String, placeholder: String, onTextSubmitted: (String) -> Unit) {
        textInputActions.add(TextInputAction(id, label, placeholder, onTextSubmitted))
    }

}

expect fun getNotificationProvider(): NotificationProvider
