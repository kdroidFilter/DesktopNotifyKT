package com.kdroid.composenotification.platform.windows.utils

import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg

fun registerBasicAUMID(aumid: String, displayName: String, iconUri: String): Boolean {
    val rootKeyPath = "Software\\Classes\\AppUserModelId"
    val aumidKeyPath = "$rootKeyPath\\$aumid"

    try {
        // Create or open the root key
        Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, rootKeyPath)
        // Create or open the AUMID key
        Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, aumidKeyPath)
        // Set the DisplayName value
        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, aumidKeyPath, "DisplayName", displayName)
        // Set the IconUri value if provided
        if (iconUri.isNotEmpty()) {
            Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, aumidKeyPath, "IconUri", iconUri)
        }
        return true
    } catch (e: Exception) {
        println("Exception in registerBasicAUMID: ${e.message}")
        return false
    }
}