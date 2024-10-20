package com.kdroid.composenotification

import com.kdroid.composenotification.builder.Notification
import com.kdroid.composenotification.models.DismissalReason
import com.kdroid.kmplog.*
import java.io.File
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

private const val RESOURCE_DIR = "src/jvmTest/resources/"
private const val DEFAULT_APP_ICON = "default_icon.ico"
private const val DEFAULT_LARGE_IMAGE = "default_icon.png"
private const val TAG = "ComposeNotification"

fun main() {
    createSwingApp()
}

fun createSwingApp() {
    Log.d(TAG, "Creating Swing application...")
    Log.d(TAG, "Current working directory: ${System.getProperty("user.dir")}")
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

private fun getAbsolutePath(relativePath: String): String? {
    val file = File(RESOURCE_DIR, relativePath)
    return if (file.exists()) {
        file.absolutePath
    } else {
        Log.w(TAG, "Resource not found: ${file.path}.")
        null
    }
}

private fun getAbsolutePathOrDefault(relativePath: String, defaultPath: String): String {
    return getAbsolutePath(relativePath) ?: run {
        Log.w(TAG, "Using default resource for: $relativePath")
        getAbsolutePath(defaultPath) ?: ""
    }
}

private fun createNotification() {
    Log.d(TAG, "Creating notification...")

    // Convertir les chemins relatifs en absolus ou utiliser des valeurs par défaut
    val appIconPath = getAbsolutePathOrDefault("icon.ico", DEFAULT_APP_ICON)
    val largeImagePath = getAbsolutePathOrDefault("icon.png", DEFAULT_LARGE_IMAGE)

    Notification(
        appName = "My Custom App",
        appIconPath = appIconPath,
        title = "Hello World!",
        message = "This is a test notification with custom app name and icon.",
        largeImagePath = largeImagePath
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
                else -> Log.w(TAG, "Notification dismissed for unknown reason: $reason")
            }
        }
        onFailed {
            Log.e(TAG, "Notification failed to display.")
        }
    }
}
