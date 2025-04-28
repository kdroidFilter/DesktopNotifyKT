package io.github.kdroidfilter.knotify.platform.windows.nativeintegration

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.win32.W32APIOptions

internal interface ExtendedUser32 : User32 {
    companion object {
        val INSTANCE: ExtendedUser32 = Native.load(
            "user32",
            ExtendedUser32::class.java,
            W32APIOptions.DEFAULT_OPTIONS // Use default options
        )
         
    }

    // Definition of MsgWaitForMultipleObjects
    fun MsgWaitForMultipleObjects(
        nCount: Int,
        pHandles: Pointer,
        bWaitAll: Boolean,
        dwMilliseconds: Int,
        dwWakeMask: Int
    ): Int


}