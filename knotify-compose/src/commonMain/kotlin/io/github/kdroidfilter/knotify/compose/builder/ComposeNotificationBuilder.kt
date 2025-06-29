package io.github.kdroidfilter.knotify.compose.builder

import androidx.compose.runtime.Composable
import io.github.kdroidfilter.knotify.builder.ExperimentalNotificationsApi
import io.github.kdroidfilter.knotify.builder.Notification
import io.github.kdroidfilter.knotify.builder.notification
import io.github.kdroidfilter.knotify.model.DismissalReason
import io.github.kdroidfilter.knotify.model.TextInputAction
import io.github.kdroidfilter.knotify.compose.utils.ComposableIconRenderer
import io.github.kdroidfilter.knotify.compose.utils.IconRenderProperties

/**
 * A wrapper for notification building that supports Composable content for large images and small icons.
 */
class ComposeNotificationWrapper(
    var title: String = "",
    var message: String = "",
    var largeImageComposable: (@Composable () -> Unit)? = null,
    var smallIconComposable: (@Composable () -> Unit)? = null,
    var onActivated: (() -> Unit)? = null,
    var onDismissed: ((DismissalReason) -> Unit)? = null,
    var onFailed: (() -> Unit)? = null
) {
    private val _buttons = mutableListOf<ButtonInfo>()
    private val _textInputActions = mutableListOf<TextInputInfo>()

    /**
     * List of buttons added to the notification.
     */
    val buttons: List<ButtonInfo> get() = _buttons

    /**
     * List of text input actions added to the notification.
     */
    val textInputActions: List<TextInputInfo> get() = _textInputActions

    /**
     * Adds a button to the notification.
     *
     * @param title The title of the button.
     * @param onClick Callback that is invoked when the button is clicked.
     */
    fun button(title: String, onClick: () -> Unit) {
        _buttons.add(ButtonInfo(title, onClick))
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
        _textInputActions.add(TextInputInfo(id, label, placeholder, onTextSubmitted))
    }

    /**
     * Information about a button in the notification.
     */
    data class ButtonInfo(val title: String, val onClick: () -> Unit)

    /**
     * Information about a text input action in the notification.
     */
    data class TextInputInfo(val id: String, val label: String, val placeholder: String, val onTextSubmitted: (String) -> Unit)
}

/**
 * Creates a notification with customizable settings, including support for Composable content as the large image and small icon.
 *
 * @param title The title of the notification. Defaults to an empty string.
 * @param message The message of the notification. Defaults to an empty string.
 * @param largeIcon A Composable function that renders the large image for the notification. Can be null.
 * @param smallIcon A Composable function that renders the small icon for the notification. Can be null.
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
    largeIcon: (@Composable () -> Unit)? = null,
    smallIcon: (@Composable () -> Unit)? = null,
    onActivated: (() -> Unit)? = null,
    onDismissed: ((DismissalReason) -> Unit)? = null,
    onFailed: (() -> Unit)? = null,
    builderAction: ComposeNotificationWrapper.() -> Unit = {}
): Notification {
    val wrapper = ComposeNotificationWrapper(title, message, largeIcon, smallIcon, onActivated, onDismissed, onFailed)
    wrapper.builderAction()

    // If we have a Composable for the large image, render it to a file
    val largeIconPath = wrapper.largeImageComposable?.let {
        ComposableIconRenderer.renderComposableToPngFile(
            IconRenderProperties.withoutScalingAndAliasing(),
            it
        )
    }

    // If we have a Composable for the small icon, render it to a file
    val smallIconPath = wrapper.smallIconComposable?.let {
        ComposableIconRenderer.renderComposableToPngFile(
            IconRenderProperties.withoutScalingAndAliasing(64, 64),
            it
        )
    }

    // Create a notification using the standard notification function
    return notification(
        title = wrapper.title,
        message = wrapper.message,
        largeIcon = largeIconPath,
        smallIcon = smallIconPath,
        onActivated = wrapper.onActivated,
        onDismissed = wrapper.onDismissed,
        onFailed = wrapper.onFailed
    ) {
        // Add buttons from the wrapper
        wrapper.buttons.forEach { buttonInfo ->
            this.button(buttonInfo.title, buttonInfo.onClick)
        }

        // Add text input actions from the wrapper
        wrapper.textInputActions.forEach { textInputInfo ->
            this.textInput(
                id = textInputInfo.id,
                label = textInputInfo.label,
                placeholder = textInputInfo.placeholder,
                onTextSubmitted = textInputInfo.onTextSubmitted
            )
        }
    }
}

/**
 * Creates and immediately sends a notification with customizable settings, including support for Composable content as the large image and small icon.
 *
 * @param title The title of the notification. Defaults to an empty string.
 * @param message The message of the notification. Defaults to an empty string.
 * @param largeIcon A Composable function that renders the large image for the notification. Can be null.
 * @param smallIcon A Composable function that renders the small icon for the notification. Can be null.
 * @param onActivated Callback that is invoked when the notification is activated.
 * @param onDismissed Callback that is invoked when the notification is dismissed.
 * @param onFailed Callback that is invoked when the notification fails to display.
 * @param builderAction A DSL block that customizes the notification options and actions.
 * @return A Notification object that can be hidden or manipulated later.
 */
@ExperimentalNotificationsApi
suspend fun sendComposeNotification(
    title: String = "",
    message: String = "",
    largeIcon: (@Composable () -> Unit)? = null,
    smallIcon: (@Composable () -> Unit)? = null,
    onActivated: (() -> Unit)? = null,
    onDismissed: ((DismissalReason) -> Unit)? = null,
    onFailed: (() -> Unit)? = null,
    builderAction: ComposeNotificationWrapper.() -> Unit = {}
): Notification {
    val notification = notification(title, message, largeIcon, smallIcon, onActivated, onDismissed, onFailed, builderAction)
    notification.send()
    return notification
}
