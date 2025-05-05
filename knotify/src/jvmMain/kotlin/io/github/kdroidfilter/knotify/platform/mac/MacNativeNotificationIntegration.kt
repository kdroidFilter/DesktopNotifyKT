package io.github.kdroidfilter.knotify.platform.mac

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Callback

/**
 * Interface for integrating native macOS notification functionalities.
 * This uses JNA to access the macOS Notification Center API.
 */
interface MacNativeNotificationIntegration : Library {
    companion object {
        // Load the native library
        val INSTANCE: MacNativeNotificationIntegration by lazy {
            try {
                Native.load("MacNotification", MacNativeNotificationIntegration::class.java)
            } catch (e: UnsatisfiedLinkError) {
                // Fallback to a dummy implementation if the native library is not available
                DummyMacNotificationIntegration()
            }
        }
    }

    /**
     * Creates a notification with the specified title, body, and icon.
     *
     * @param title The title of the notification
     * @param body The body text of the notification
     * @param iconPath The path to the icon image (can be null)
     * @return A pointer to the created notification, or null if creation failed
     */
    fun create_notification(title: String, body: String, iconPath: String?): Pointer?

    /**
     * Adds a button to the notification.
     *
     * @param notification The notification to add the button to
     * @param buttonId The ID of the button
     * @param buttonLabel The label text for the button
     * @param callback The callback to invoke when the button is clicked
     * @param userData User data to pass to the callback
     */
    fun add_button_to_notification(
        notification: Pointer?,
        buttonId: String,
        buttonLabel: String,
        callback: ButtonClickedCallback?,
        userData: Pointer?
    )

    /**
     * Sets a callback to be invoked when the notification is clicked.
     *
     * @param notification The notification to set the callback for
     * @param callback The callback to invoke when the notification is clicked
     * @param userData User data to pass to the callback
     */
    fun set_notification_clicked_callback(
        notification: Pointer?,
        callback: NotificationClickedCallback?,
        userData: Pointer?
    )

    /**
     * Sets a callback to be invoked when the notification is closed.
     *
     * @param notification The notification to set the callback for
     * @param callback The callback to invoke when the notification is closed
     * @param userData User data to pass to the callback
     */
    fun set_notification_closed_callback(
        notification: Pointer?,
        callback: NotificationClosedCallback?,
        userData: Pointer?
    )

    /**
     * Sets an image for the notification.
     *
     * @param notification The notification to set the image for
     * @param imagePath The path to the image file
     */
    fun set_notification_image(notification: Pointer?, imagePath: String)

    /**
     * Sends the notification.
     *
     * @param notification The notification to send
     * @return 0 on success, non-zero on failure
     */
    fun send_notification(notification: Pointer?): Int

    /**
     * Hides/removes the notification.
     *
     * @param notification The notification to hide
     */
    fun hide_notification(notification: Pointer?)

    /**
     * Cleans up resources associated with the notification.
     *
     * @param notification The notification to clean up
     */
    fun cleanup_notification(notification: Pointer?)
}

/**
 * Callback interface for notification clicked events.
 */
interface NotificationClickedCallback : Callback {
    fun invoke(notification: Pointer?, userData: Pointer?)
}

/**
 * Callback interface for notification closed events.
 */
interface NotificationClosedCallback : Callback {
    fun invoke(notification: Pointer?, userData: Pointer?)
}

/**
 * Callback interface for button clicked events.
 */
interface ButtonClickedCallback : Callback {
    fun invoke(notification: Pointer?, buttonId: String?, userData: Pointer?)
}

/**
 * Dummy implementation of MacNativeNotificationIntegration for when the native library is not available.
 * This allows the application to continue running without crashing, but notifications won't be displayed.
 */
private class DummyMacNotificationIntegration : MacNativeNotificationIntegration {
    override fun create_notification(title: String, body: String, iconPath: String?): Pointer? = null
    
    override fun add_button_to_notification(
        notification: Pointer?,
        buttonId: String,
        buttonLabel: String,
        callback: ButtonClickedCallback?,
        userData: Pointer?
    ) {}
    
    override fun set_notification_clicked_callback(
        notification: Pointer?,
        callback: NotificationClickedCallback?,
        userData: Pointer?
    ) {}
    
    override fun set_notification_closed_callback(
        notification: Pointer?,
        callback: NotificationClosedCallback?,
        userData: Pointer?
    ) {}
    
    override fun set_notification_image(notification: Pointer?, imagePath: String) {}
    
    override fun send_notification(notification: Pointer?): Int = -1
    
    override fun hide_notification(notification: Pointer?) {}
    
    override fun cleanup_notification(notification: Pointer?) {}
}