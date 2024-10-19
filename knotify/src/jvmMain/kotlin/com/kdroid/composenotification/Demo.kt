package com.kdroid.composenotification

import com.kdroid.composenotification.models.DismissalReason
import com.kdroid.composenotification.models.Notification

// Main.kt
fun main() {
    val iconPath = "C:\\Users\\Elyahou Gambache\\IdeaProjects\\KNotify\\knotify\\src\\jvmTest\\resources\\icon.ico"

    Notification {
        appName = "My Custom App"
        appIconPath = iconPath // Set to null or empty string if no custom icon

        title = "Hello World!"
        message = "This is a test notification with custom app name and icon."
        largeImagePath = "C:\\Users\\Elyahou Gambache\\IdeaProjects\\KNotify\\knotify\\src\\jvmTest\\resources\\icon.png" // Set to null if no image

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
                DismissalReason.UserCanceled -> println("User dismissed the notification.")
                DismissalReason.ApplicationHidden -> println("Notification hidden by application.")
                DismissalReason.TimedOut -> println("Notification timed out.")
                else -> println("Notification dismissed for unknown reason.")
            }
        }

        onFailed {
            println("Notification failed to display.")
        }
    }
}

