package com.kdroid.composenotification

import com.kdroid.composenotification.builder.Notification
import com.kdroid.composenotification.models.DismissalReason
import com.kdroid.kmplog.*
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

private const val ICON_PATH = "src/jvmTest/resources/icon.ico"
private const val LARGE_IMAGE_PATH = "src/jvmTest/resources/icon.png"
private const val TAG = "ComposeNotification"

fun main() {
    createSwingApp()
}

fun createSwingApp() {
    Log.d(TAG, "Creating Swing application...")
    val frame = JFrame("Démo Swing Kotlin").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setSize(400, 300)
        contentPane = JPanel().apply {
            add(createSendNotificationButton())
        }
        isVisible = true
    }
}

private fun createSendNotificationButton(): JButton {
    val sendNotificationButton = JButton("Cliquez-moi!")
    sendNotificationButton.addActionListener {
        Log.i(TAG, "Send notification button clicked.")
        createNotification()
    }
    return sendNotificationButton
}

private fun createNotification() {
    Log.d(TAG, "Creating notification...")
    Notification(
        appName = "My Custom App",
        appIconPath = ICON_PATH,
        title = "Hello World!",
        message = "This is a test notification with custom app name and icon.",
        largeImagePath = LARGE_IMAGE_PATH
    ) {
        Button("Button 1") {
            Log.d(TAG, "Button 1 clicked.")
        }
        Button("Button 2") {
            Log.d(TAG, "Button 2 clicked.")
        }
        onActivated {
            Log.i(TAG, "Notification activated.")
        }
        onDismissed { reason ->
            when (reason) {
                DismissalReason.UserCanceled -> Log.w(TAG, "User dismissed the notification.")
                DismissalReason.ApplicationHidden -> Log.w(TAG, "Notification hidden by application.")
                DismissalReason.TimedOut -> Log.w(TAG, "Notification timed out.")
                else -> Log.w(TAG, "Notification dismissed for unknown reason. : $reason")
            }
        }
        onFailed {
            Log.e(TAG, "Notification failed to display.")
        }
    }
}