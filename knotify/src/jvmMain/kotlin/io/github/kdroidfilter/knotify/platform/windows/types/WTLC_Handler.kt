package io.github.kdroidfilter.knotify.platform.windows.types

import com.sun.jna.Pointer
import com.sun.jna.Structure
import io.github.kdroidfilter.knotify.platform.windows.callbacks.ToastActivatedCallback
import io.github.kdroidfilter.knotify.platform.windows.callbacks.ToastActivatedActionCallback
import io.github.kdroidfilter.knotify.platform.windows.callbacks.ToastActivatedInputCallback
import io.github.kdroidfilter.knotify.platform.windows.callbacks.ToastDismissedCallback
import io.github.kdroidfilter.knotify.platform.windows.callbacks.ToastFailedCallback

/**
 * JNA structure representing the WTLC_Handler struct from wintoastlibc.h
 */
internal class WTLC_Handler : Structure() {
    @JvmField var version: Long = 0
    @JvmField var userData: Pointer? = null
    @JvmField var toastActivated: ToastActivatedCallback? = null
    @JvmField var toastActivatedAction: ToastActivatedActionCallback? = null
    @JvmField var toastDismissed: ToastDismissedCallback? = null
    @JvmField var toastFailed: ToastFailedCallback? = null
    @JvmField var toastActivatedInput: ToastActivatedInputCallback? = null

    override fun getFieldOrder(): List<String> {
        return listOf(
            "version",
            "userData",
            "toastActivated",
            "toastActivatedAction",
            "toastDismissed",
            "toastFailed",
            "toastActivatedInput"
        )
    }

    companion object {
        // Version constant for WinToastLibC v0.5.0
        const val TOAST_ACTIVATED_INPUT_VERSION: Long = 0x0005000000000000
    }
}
