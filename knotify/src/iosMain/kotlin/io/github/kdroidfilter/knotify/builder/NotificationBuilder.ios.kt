package io.github.kdroidfilter.knotify.builder

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

actual fun getNotificationProvider(): NotificationProvider = iosNotificationProvider()

class iosNotificationProvider() : NotificationProvider {
    override val hasPermissionState = mutableStateOf(true)

    override fun sendNotification(builder: NotificationBuilder) {

    }

    override fun hideNotification(builder: NotificationBuilder) {

    }

    override fun hasPermission(): Boolean = true
}