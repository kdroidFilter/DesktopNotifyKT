package com.kdroid.composenotification.models

import com.kdroid.composenotification.NotificationProvider
import com.kdroid.composenotification.platform.linux.LinuxNotificationProvider
import com.kdroid.composenotification.platform.windows.service.WindowsNotificationProvider
import com.kdroid.composenotification.utils.OsUtils

// Notification.kt
fun Notification(builderAction: NotificationBuilder.() -> Unit) {
    val builder = NotificationBuilder()
    builder.builderAction()
    builder.send()
}


// NotificationBuilder.kt
class NotificationBuilder {
    var appName: String = "NotificationExample"
    var appIconPath: String? = null
    var title: String = ""
    var message: String = ""
    var imagePath: String? = null
    internal val buttons = mutableListOf<Button>()

    internal var onActivated: (() -> Unit)? = null
    internal var onDismissed: ((DismissalReason) -> Unit)? = null
    internal var onFailed: (() -> Unit)? = null

    fun button(label: String, onClick: () -> Unit) {
        buttons.add(Button(label, onClick))
    }

    fun onActivated(callback: () -> Unit) {
        this.onActivated = callback
    }

    fun onDismissed(callback: (DismissalReason) -> Unit) {
        this.onDismissed = callback
    }

    fun onFailed(callback: () -> Unit) {
        this.onFailed = callback
    }

    fun send() {
        val notificationProvider: NotificationProvider = when {
            OsUtils.isLinux() -> LinuxNotificationProvider()
            OsUtils.isWindows() -> WindowsNotificationProvider()
            else -> throw UnsupportedOperationException("Unsupported OS")
        }

        notificationProvider.sendNotification(this)
    }
}

// DismissalReason.kt
enum class DismissalReason {
    UserCanceled,
    ApplicationHidden,
    TimedOut,
    Unknown
}

