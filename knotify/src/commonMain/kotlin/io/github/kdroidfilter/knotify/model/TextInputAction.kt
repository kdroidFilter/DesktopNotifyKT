package io.github.kdroidfilter.knotify.model

/**
 * Represents a text input action in a notification.
 *
 * @param id The unique identifier for this text input action
 * @param label The text displayed on the text input button
 * @param placeholder The placeholder text displayed in the text input field
 * @param onTextSubmitted Callback that is invoked when text is submitted, with the submitted text as parameter
 */
data class TextInputAction(
    val id: String,
    val label: String,
    val placeholder: String,
    val onTextSubmitted: (String) -> Unit
)