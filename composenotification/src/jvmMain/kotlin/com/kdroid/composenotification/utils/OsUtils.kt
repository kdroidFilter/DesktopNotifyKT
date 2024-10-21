package com.kdroid.composenotification.utils

internal object OsUtils {
    fun getOsName(): String {
        return System.getProperty("os.name").lowercase()
    }

    fun isLinux(): Boolean {
        return getOsName().contains("linux")
    }

    fun isWindows(): Boolean {
        return getOsName().contains("windows")
    }

    fun isMac(): Boolean {
        return getOsName().contains("mac")
    }
}