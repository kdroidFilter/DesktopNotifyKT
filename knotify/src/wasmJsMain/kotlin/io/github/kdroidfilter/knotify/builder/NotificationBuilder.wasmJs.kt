package io.github.kdroidfilter.knotify.builder

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.github.kdroidfilter.knotify.model.DismissalReason
import org.w3c.notifications.*
import org.w3c.notifications.Notification.Companion.permission
import org.w3c.notifications.Notification.Companion.requestPermission as notificationRequestPermission
import kotlin.js.Promise

// Actual implementation for the JS platform using WASM
actual fun getNotificationProvider(): NotificationProvider = WasmJsNotificationProvider()

class WasmJsNotificationProvider : NotificationProvider {
    private val _hasPermissionState = mutableStateOf(hasPermission())

    override val hasPermissionState: State<Boolean>
        get() = _hasPermissionState

    override fun updatePermissionState(isGranted: Boolean) {
        _hasPermissionState.value = isGranted
    }

    override fun sendNotification(builder: NotificationBuilder) {
        if (hasPermission()) {
            val options = NotificationOptions(
                body = builder.message,
                icon = builder.largeImagePath
                // Note: Actions (buttons) are not supported in the same way in WASM JS
                // The Web Notifications API in WASM JS has different type requirements
                // than regular JS. For now, we'll handle button clicks through the main
                // notification click event.
            )

            val notification = Notification(builder.title, options)

            // Handle notification click event
            notification.onclick = {
                // If there are buttons, we'll just trigger the first button's action
                // This is a limitation of the current WASM JS implementation
                if (builder.buttons.isNotEmpty()) {
                    builder.buttons.first().onClick()
                } else {
                    builder.onActivated?.invoke()
                }
                // Close the notification
                notification.close()
            }

            // Handle notification error event
            notification.onerror = {
                builder.onFailed?.invoke()
            }
        } else {
            // Permission not granted
            builder.onFailed?.invoke()
        }
    }

    override fun hideNotification(builder: NotificationBuilder) {
        // In web notifications, there's no direct way to hide a notification programmatically
        // The user must close it manually, or it will close automatically after a timeout
        // This is a limitation of the Web Notifications API
    }

    override fun hasPermission(): Boolean {
        return permission == NotificationPermission.GRANTED
    }

    override fun requestPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        // First check if we already have permission
        if (hasPermission()) {
            updatePermissionState(true)
            onGranted()
            return
        }

        // If not, request permission
        try {
            // This will trigger the browser's permission request dialog
            // In WASM JS, we can't directly use the Promise API due to type compatibility issues
            // So we'll just call notificationRequestPermission() and assume it was denied
            notificationRequestPermission()

            // Since we can't properly handle the Promise in WASM JS,
            // we'll default to the denied state
            updatePermissionState(false)
            onDenied()
        } catch (e: Exception) {
            // If there's an error, default to denied
            updatePermissionState(false)
            onDenied()
        }
    }
}
