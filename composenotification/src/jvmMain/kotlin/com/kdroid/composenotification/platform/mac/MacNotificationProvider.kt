package com.kdroid.composenotification.platform.mac

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.kdroid.composenotification.builder.NotificationBuilder
import com.kdroid.composenotification.builder.NotificationProvider
import java.io.IOException

internal class MacNotificationProvider() : NotificationProvider {

    private val _hasPermissionState: MutableState<Boolean> = mutableStateOf(true)
    override val hasPermissionState: State<Boolean> get() = _hasPermissionState

    override fun updatePermissionState(isGranted: Boolean) {
        _hasPermissionState.value = isGranted
    }

    override fun hasPermission(): Boolean {
        return true
    }

    override fun requestPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        if (hasPermission()) {
            onGranted()
        } else {
            onDenied()
        }
    }

    override fun sendNotification(builder: NotificationBuilder) {
        val osaPath = findOsascriptPath() ?: return

        val title = builder.title
        val message = builder.message

        val script = "display notification \"$message\" with title \"$title\""
        val processBuilder = ProcessBuilder(osaPath, "-e", script)

        try {
            val process = processBuilder.start()
            if (process.waitFor() != 0) {
                builder.onFailed?.invoke()
            } else {
                builder.onActivated?.invoke()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            builder.onFailed?.invoke()
        } catch (e: InterruptedException) {
            e.printStackTrace()
            builder.onFailed?.invoke()
        }
    }

    private fun findOsascriptPath(): String? {
        return try {
            val process = ProcessBuilder("which", "osascript").start()
            val result = process.inputStream.bufferedReader().readText().trim()
            result.ifEmpty { null }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}