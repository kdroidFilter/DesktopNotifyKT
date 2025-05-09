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

    fun my_notify_init(app_name: String): Boolean

    fun create_notification(summary: String, body: String, icon_path: String): Pointer?

    fun add_button_to_notification(notification: Pointer?, button_id: String, button_label: String, callback: NotifyActionCallback?, user_data: Pointer?)

    fun send_notification(notification: Pointer?): Int

    fun set_image_from_pixbuf(notification: Pointer?, pixbuf: Pointer?)

    fun load_pixbuf_from_file(image_path: String): Pointer?

    fun set_notification_closed_callback(notification: Pointer?, callback: NotifyClosedCallback, user_data: Pointer?)

    fun set_notification_clicked_callback(notification: Pointer?, callback: NotifyActionCallback, user_data: Pointer?)

    fun cleanup_notification()

    fun run_main_loop()

    fun quit_main_loop()

}
