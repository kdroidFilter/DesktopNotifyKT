package io.github.kdroidfilter.knotify.platform.linux

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

/**
 * Interface for integrating native Linux notification functionalities.
 */
interface LinuxNativeNotificationIntegration : Library {
    companion object {
        val INSTANCE: LinuxNativeNotificationIntegration = Native.load("notification", LinuxNativeNotificationIntegration::class.java)
    }

    /**
     * Set debug mode for the native library
     * @param enable 1 to enable debug logs, 0 to disable
     */
    fun set_debug_mode(enable: Int)

    /**
     * Initialize the notification library with the application name
     * @param app_name The name of the application
     * @return true if initialization was successful, false otherwise
     */
    fun my_notify_init(app_name: String): Boolean

    /**
     * Create a new notification with an icon
     * @param summary The notification title
     * @param body The notification message
     * @param icon_path Path to the icon file
     * @return Pointer to the created notification or null if failed
     */
    fun create_notification(summary: String, body: String, icon_path: String): Pointer?

    /**
     * Add a button to the notification
     * @param notification Pointer to the notification
     * @param button_id ID of the button
     * @param button_label Label of the button
     * @param callback Callback to be called when the button is clicked
     * @param user_data User data to be passed to the callback
     */
    fun add_button_to_notification(notification: Pointer?, button_id: String, button_label: String, callback: NotifyActionCallback?, user_data: Pointer?)

    /**
     * Send the notification
     * @param notification Pointer to the notification
     * @return 0 if successful, non-zero otherwise
     */
    fun send_notification(notification: Pointer?): Int

    /**
     * Set image from GdkPixbuf
     * @param notification Pointer to the notification
     * @param pixbuf Pointer to the GdkPixbuf
     */
    fun set_image_from_pixbuf(notification: Pointer?, pixbuf: Pointer?)

    /**
     * Load a GdkPixbuf from a file
     * @param image_path Path to the image file
     * @return Pointer to the loaded GdkPixbuf or null if failed
     */
    fun load_pixbuf_from_file(image_path: String): Pointer?

    /**
     * Set callback for notification close
     * @param notification Pointer to the notification
     * @param callback Callback to be called when the notification is closed
     * @param user_data User data to be passed to the callback
     */
    fun set_notification_closed_callback(notification: Pointer?, callback: NotifyClosedCallback, user_data: Pointer?)

    /**
     * Set callback for notification click
     * @param notification Pointer to the notification
     * @param callback Callback to be called when the notification is clicked
     * @param user_data User data to be passed to the callback
     */
    fun set_notification_clicked_callback(notification: Pointer?, callback: NotifyActionCallback, user_data: Pointer?)

    /**
     * Clean up resources
     */
    fun cleanup_notification()

    /**
     * Start the main loop to handle events
     */
    fun run_main_loop()

    /**
     * Stop the main loop
     */
    fun quit_main_loop()
}
