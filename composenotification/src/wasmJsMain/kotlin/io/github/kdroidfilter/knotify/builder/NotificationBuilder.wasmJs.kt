package io.github.kdroidfilter.knotify.builder

import androidx.compose.runtime.State
import org.w3c.notifications.GRANTED
import org.w3c.notifications.NotificationPermission

// Actual implementation for the JS platform using WASM
actual fun getNotificationProvider(): NotificationProvider = WasmJsNotificationProvider()

class WasmJsNotificationProvider : NotificationProvider {
    override val hasPermissionState: State<Boolean>
        get() = TODO("Not yet implemented")

    override fun sendNotification(builder: NotificationBuilder) {
       TODO()
    }

    override fun hasPermission(): Boolean {
        TODO()
    }

    override fun requestPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
       TODO()
    }
}
