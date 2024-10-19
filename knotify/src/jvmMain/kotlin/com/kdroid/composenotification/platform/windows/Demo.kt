package com.kdroid.composenotification.platform.windows

import com.kdroid.composenotification.platform.windows.constants.WTLC_DismissalReason_Constants
import com.kdroid.composenotification.platform.windows.models.sendNotification

fun main() {
    sendNotification {
        appName = "My Custom App"
        appIconPath = "C:\\Users\\Elyahou Gambache\\CLionProjects\\tray\\icon.ico" // Set to null or empty string if no custom icon

        title = "Hello World!"
        message = "This is a test notification with custom app name and icon."
        imagePath = "C:\\Users\\Elyahou Gambache\\Pictures/kdroid.png" // Set to null if no image

        button("Button 1") {
            // Callback for Button 1
            println("Button 1 clicked.")
        }
        button("Button 2") {
            // Callback for Button 2
            println("Button 2 clicked.")
        }

        onActivated {
            println("Notification activated.")
        }

        onDismissed { reason ->
            when (reason) {
                WTLC_DismissalReason_Constants.UserCanceled -> println("User dismissed the notification.")
                WTLC_DismissalReason_Constants.ApplicationHidden -> println("Notification hidden by application.")
                WTLC_DismissalReason_Constants.TimedOut -> println("Notification timed out.")
                else -> println("Notification dismissed for unknown reason.")
            }
        }

        onFailed {
            println("Notification failed to display.")
        }
    }
}
