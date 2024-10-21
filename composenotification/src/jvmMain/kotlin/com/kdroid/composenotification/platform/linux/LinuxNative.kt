package com.kdroid.composenotification.platform.linux

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

// Définir une interface pour ta bibliothèque
interface LinuxNotificationLibrary : Library {
    companion object {
        val INSTANCE: LinuxNotificationLibrary = Native.load("notification", LinuxNotificationLibrary::class.java)
    }

    fun create_notification(summary: String, body: String, icon_path: String): Pointer?

    fun add_button_to_notification(notification: Pointer?, button_id: String, button_label: String, callback: NotifyActionCallback?, user_data: Pointer?)

    fun send_notification(notification: Pointer?): Int

    fun set_image_from_pixbuf(notification: Pointer?, pixbuf: Pointer?)

    fun cleanup_notification()

    fun run_main_loop()

    fun quit_main_loop()
    // Nouvelle fonction ajoutée pour charger un GdkPixbuf
    fun load_pixbuf_from_file(image_path: String): Pointer?
}


