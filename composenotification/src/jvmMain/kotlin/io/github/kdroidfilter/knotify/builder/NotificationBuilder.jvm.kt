package io.github.kdroidfilter.knotify.builder

import io.github.kdroidfilter.knotify.platform.linux.LinuxNotificationProvider
import io.github.kdroidfilter.knotify.platform.mac.MacNotificationProvider
import io.github.kdroidfilter.knotify.platform.windows.provider.WindowsNotificationProvider
import io.github.kdroidfilter.platformtools.OperatingSystem
import io.github.kdroidfilter.platformtools.getOperatingSystem

actual fun getNotificationProvider(): NotificationProvider {
    val os = getOperatingSystem()
    return when (os) {
        OperatingSystem.LINUX -> LinuxNotificationProvider()
        OperatingSystem.WINDOWS -> WindowsNotificationProvider()
        OperatingSystem.MACOS -> MacNotificationProvider()
        else -> throw UnsupportedOperationException("Unsupported OS")
    }
}

