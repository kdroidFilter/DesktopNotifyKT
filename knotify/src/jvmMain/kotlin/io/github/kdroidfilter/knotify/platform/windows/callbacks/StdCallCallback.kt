
package io.github.kdroidfilter.knotify.platform.windows.callbacks

import com.sun.jna.Pointer
import com.sun.jna.win32.StdCallLibrary

/**
 * Callback interfaces for Windows Toast notifications
 */
internal interface ToastActivatedCallback : StdCallLibrary.StdCallCallback {
    fun invoke(userData: Pointer?)
}

internal interface ToastActivatedActionCallback : StdCallLibrary.StdCallCallback {
    fun invoke(userData: Pointer?, actionIndex: Int)
}

internal interface ToastDismissedCallback : StdCallLibrary.StdCallCallback {
    fun invoke(userData: Pointer?, state: Int)
}

internal interface ToastFailedCallback : StdCallLibrary.StdCallCallback {
    fun invoke(userData: Pointer?)
}
