package com.kdroid.composenotification.builder

import com.kdroid.composenotification.platform.linux.LinuxNotificationProvider
import com.kdroid.composenotification.platform.mac.MacNotificationProvider
import com.kdroid.composenotification.platform.windows.provider.WindowsNotificationProvider
import com.kdroid.composenotification.utils.OsUtils

actual fun getNotificationProvider(): NotificationProvider {
    return when {
        OsUtils.isLinux() -> LinuxNotificationProvider()
        OsUtils.isWindows() -> WindowsNotificationProvider()
        OsUtils.isMac() -> MacNotificationProvider()
        else -> throw UnsupportedOperationException("Unsupported OS")
    }
}

