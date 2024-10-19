// LinuxNotificationProvider.kt
package com.kdroid.composenotification.platform.linux

import com.kdroid.composenotification.NotificationProvider
import com.kdroid.composenotification.models.NotificationBuilder
import com.sun.jna.Pointer

class LinuxNotificationProvider : NotificationProvider {
    override fun sendNotification(builder: NotificationBuilder) {
        val lib = NotificationLibrary.INSTANCE

        val iconPath = builder.appIconPath ?: ""
        val notification = lib.create_notification(builder.title, builder.message, iconPath)
        if (notification == null) {
            println("Failed to create notification.")
            builder.onFailed?.invoke()
            return
        }

        builder.largeImagePath?.let {
            val pixbufPointer = lib.load_pixbuf_from_file(it)
            if (pixbufPointer != Pointer.NULL) {
                lib.set_image_from_pixbuf(notification, pixbufPointer)
            } else {
                println("Unable to load image: $it")
            }
        }

        builder.buttons.forEach { button ->
            lib.add_button_to_notification(notification, button.label, button.label, { _, action, _ ->
                if (action == button.label) {
                    button.onClick.invoke()
                }
            }, Pointer.NULL)
        }

        val result = lib.send_notification(notification)
        if (result == 0) {
            println("Notification sent successfully.")
            builder.onActivated?.invoke()
        } else {
            println("Failed to send notification.")
            builder.onFailed?.invoke()
        }

        lib.run_main_loop()
        lib.cleanup_notification()
    }
}
