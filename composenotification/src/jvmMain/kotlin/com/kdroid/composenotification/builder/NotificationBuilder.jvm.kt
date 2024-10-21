package com.kdroid.composenotification.builder

import com.kdroid.composenotification.platform.linux.LinuxNotificationProvider
import com.kdroid.composenotification.platform.windows.provider.WindowsNotificationProvider
import com.kdroid.composenotification.utils.OsUtils

// jvmMain/src/jvmMain/kotlin/JvmNotificationProvider.kt
actual fun getNotificationProvider(context: Any?): NotificationProvider {
    return when {
        OsUtils.isLinux() -> LinuxNotificationProvider()
        OsUtils.isWindows() -> WindowsNotificationProvider()
        else -> throw UnsupportedOperationException("Unsupported OS")
    }
}

