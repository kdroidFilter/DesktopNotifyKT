package com.kdroid.composenotification.platform.windows

import com.kdroid.composenotification.platform.windows.constants.WTLC_DismissalReason_Constants
import com.kdroid.composenotification.platform.windows.models.sendNotification
import com.kdroid.kmplog.*

fun main() {
    sendNotification {
        appName = "My Custom App"
        appIconPath = "C:\\Users\\Elyahou Gambache\\CLionProjects\\tray\\icon.ico" // Set to null or empty string if no custom icon

        title = "Hello World!"
        message = "This is a test notification with custom app name and icon."
        imagePath = "C:\\Users\\Elyahou Gambache\\Pictures/kdroid.png" // Set to null if no image

        button("Button 1") {
            // Callback for Button 1
            Log.d("Notification", "Button 1 clicked.")
        }
        button("Button 2") {
            // Callback for Button 2
            Log.d("Notification", "Button 2 clicked.")
        }

        onActivated {
            Log.i("Notification", "Notification activated.")
        }

        onDismissed { reason ->
            when (reason) {
                WTLC_DismissalReason_Constants.UserCanceled -> Log.w("Notification", "User dismissed the notification.")
                WTLC_DismissalReason_Constants.ApplicationHidden -> Log.w("Notification", "Notification hidden by application.")
                WTLC_DismissalReason_Constants.TimedOut -> Log.w("Notification", "Notification timed out.")
                else -> Log.w("Notification", "Notification dismissed for unknown reason.")
            }
        }

        onFailed {
            Log.e("Notification", "Notification failed to display.")
        }
    }
}
