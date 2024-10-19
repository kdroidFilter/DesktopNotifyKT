// WindowsNotificationProvider.kt
package com.kdroid.composenotification.platform.windows.service

import ToastActivatedActionCallback
import ToastActivatedCallback
import ToastDismissedCallback
import ToastFailedCallback
import com.kdroid.composenotification.NotificationProvider
import com.kdroid.composenotification.models.DismissalReason
import com.kdroid.composenotification.models.NotificationBuilder
import com.kdroid.composenotification.platform.windows.constants.*
import com.kdroid.composenotification.platform.windows.nativeintegration.ExtendedUser32
import com.kdroid.composenotification.platform.windows.nativeintegration.WinToastLibC
import com.kdroid.composenotification.platform.windows.utils.registerBasicAUMID
import com.kdroid.kmplog.Log
import com.kdroid.kmplog.e
import com.kdroid.kmplog.w
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Ole32
import com.sun.jna.platform.win32.WinBase
import com.sun.jna.platform.win32.WinBase.WAIT_OBJECT_0
import com.sun.jna.platform.win32.WinError.WAIT_TIMEOUT
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import java.io.File

class WindowsNotificationProvider : NotificationProvider {
    override fun sendNotification(builder: NotificationBuilder) {
        // Initialize the COM library
        val hr = Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_APARTMENTTHREADED)
        if (COMUtils.FAILED(hr)) {
            Log.e("Notification", "Failed to initialize COM library!")
            return
        }

        try {
            val wtlc = WinToastLibC.INSTANCE

            if (!wtlc.WTLC_isCompatible()) {
                Log.e("Notification", "Your system is not compatible!")
                return
            }

            val instance = wtlc.WTLC_Instance_Create()
            if (instance == null) {
                Log.e("Notification", "Failed to create WinToast instance!")
                return
            }

            try {
                // Set the app name
                wtlc.WTLC_setAppName(instance, WString(builder.appName))

                // Create a unique AUMID based on app name
                val aumid = builder.appName.replace(" ", "") + "Toast"

                // Register the AUMID with the app display name and icon
                if (registerBasicAUMID(aumid, builder.appName, builder.appIconPath ?: "")) {
                    wtlc.WTLC_setAppUserModelId(instance, WString(aumid))
                } else {
                    // If registration failed, use the app name as AUMID
                    wtlc.WTLC_setAppUserModelId(instance, WString(builder.appName))
                }

                wtlc.WTLC_setShortcutPolicy(instance, WTLC_ShortcutPolicy_Constants.IGNORE)

                val errorRef = IntByReference(0)
                if (!wtlc.WTLC_initialize(instance, errorRef)) {
                    val errorMsg = wtlc.WTLC_strerror(errorRef.value).toString()
                    Log.e("Notification", "Initialization error: $errorMsg")
                    return
                }

                val templateType = if (builder.largeImagePath != null && File(builder.largeImagePath!!).exists()) {
                    WTLC_TemplateType_Constants.ImageAndText02
                } else {
                    WTLC_TemplateType_Constants.Text02
                }
                val template = wtlc.WTLC_Template_Create(templateType)
                if (template == null) {
                    Log.e("Notification", "Failed to create template!")
                    return
                }

                try {
                    wtlc.WTLC_Template_setTextField(
                        template,
                        WString(builder.title),
                        WTLC_TextField_Constants.FirstLine
                    )
                    wtlc.WTLC_Template_setTextField(
                        template,
                        WString(builder.message),
                        WTLC_TextField_Constants.SecondLine
                    )

                    if (builder.largeImagePath != null && File(builder.largeImagePath!!).exists()) {
                        wtlc.WTLC_Template_setImagePath(template, WString(builder.largeImagePath))
                    }

                    for (button in builder.buttons) {
                        wtlc.WTLC_Template_addAction(template, WString(button.label))
                    }

                    wtlc.WTLC_Template_setAudioOption(template, WTLC_AudioOption_Constants.Default)
                    wtlc.WTLC_Template_setExpiration(template, 30000) // 30 seconds

                    // Create event handle
                    val hEvent = Kernel32.INSTANCE.CreateEvent(null, true, false, null)
                    if (hEvent == WinBase.INVALID_HANDLE_VALUE) {
                        Log.e("Notification", "Failed to create event!")
                        return
                    }

                    try {
                        val activatedCallback = object : ToastActivatedCallback {
                            override fun invoke(userData: Pointer?) {
                                try {
                                    if (hEvent != WinBase.INVALID_HANDLE_VALUE) {
                                        builder.onActivated?.invoke()
                                        Kernel32.INSTANCE.SetEvent(hEvent)
                                    } else {
                                        Log.e("Notification", "Invalid hEvent during toast activation.")
                                    }
                                } catch (e: Exception) {
                                    Log.e("Notification", "Error during toast activation: ${e.message}")
                                }
                            }
                        }

                        val activatedActionCallback = object : ToastActivatedActionCallback {
                            override fun invoke(userData: Pointer?, actionIndex: Int) {
                                try {
                                    if (hEvent != WinBase.INVALID_HANDLE_VALUE) {
                                        builder.buttons.getOrNull(actionIndex)?.onClick?.invoke()
                                        Kernel32.INSTANCE.SetEvent(hEvent)
                                    } else {
                                        Log.e("Notification", "Invalid hEvent during toast action activation.")
                                    }
                                } catch (e: Exception) {
                                    Log.e("Notification", "Error during toast action activation: ${e.message}")
                                }
                            }
                        }

                        val dismissedCallback = object : ToastDismissedCallback {
                            override fun invoke(userData: Pointer?, state: Int) {
                                try {
                                    if (hEvent != WinBase.INVALID_HANDLE_VALUE) {
                                        val dismissalReason = when (state) {
                                            WTLC_DismissalReason_Constants.UserCanceled -> DismissalReason.UserCanceled
                                            WTLC_DismissalReason_Constants.ApplicationHidden -> DismissalReason.ApplicationHidden
                                            WTLC_DismissalReason_Constants.TimedOut -> DismissalReason.TimedOut
                                            else -> DismissalReason.Unknown
                                        }
                                        builder.onDismissed?.invoke(dismissalReason)
                                        Kernel32.INSTANCE.SetEvent(hEvent)
                                    } else {
                                        Log.e("Notification", "Invalid hEvent during toast dismissal.")
                                    }
                                } catch (e: Exception) {
                                    Log.e("Notification", "Error during toast dismissal: ${e.message}")
                                }
                            }
                        }

                        val failedCallback = object : ToastFailedCallback {
                            override fun invoke(userData: Pointer?) {
                                try {
                                    if (hEvent != WinBase.INVALID_HANDLE_VALUE) {
                                        builder.onFailed?.invoke()
                                        Kernel32.INSTANCE.SetEvent(hEvent)
                                    } else {
                                        Log.e("Notification", "Invalid hEvent during toast failure.")
                                    }
                                } catch (e: Exception) {
                                    Log.e("Notification", "Error during toast failure: ${e.message}")
                                }
                            }
                        }

                        val showResult = wtlc.WTLC_showToast(
                            instance,
                            template,
                            null,
                            activatedCallback,
                            activatedActionCallback,
                            dismissedCallback,
                            failedCallback,
                            errorRef
                        )

                        if (showResult < 0) {
                            val errorMsg = wtlc.WTLC_strerror(errorRef.value).toString()
                            Log.e("Notification", "Error showing toast: $errorMsg")
                        } else {
                            // Implement message loop to wait for callbacks
                            val startTime = Kernel32.INSTANCE.GetTickCount()
                            val timeout = 31000L // 31 seconds
                            var done = false

                            val user32 = ExtendedUser32.INSTANCE
                            val msg = WinUser.MSG()
                            val nCount = 1
                            val handleArray = Memory(Native.POINTER_SIZE.toLong() * nCount)
                            handleArray.setPointer(0, hEvent.pointer)

                            while (!done) {
                                val elapsedTime = Kernel32.INSTANCE.GetTickCount() - startTime
                                if (elapsedTime >= timeout) {
                                    Log.w("Notification", "Timeout. Exiting...")
                                    break
                                }
                                val waitTime = timeout - elapsedTime

                                val waitResult = user32.MsgWaitForMultipleObjects(
                                    nCount,
                                    handleArray,
                                    false,
                                    waitTime.toInt(),
                                    QS_ALLEVENTS or QS_ALLINPUT
                                )

                                when (waitResult) {
                                    WAIT_OBJECT_0 -> {
                                        // Event signaled
                                        done = true
                                    }

                                    WAIT_OBJECT_0 + 1 -> {
                                        // Messages in queue
                                        while (user32.PeekMessage(msg, null, 0, 0, PM_REMOVE)) {
                                            user32.TranslateMessage(msg)
                                            user32.DispatchMessage(msg)
                                        }
                                    }

                                    WAIT_TIMEOUT -> {
                                        Log.w("Notification", "Wait timeout. Exiting...")
                                        done = true
                                    }

                                    else -> {
                                        // Error occurred
                                        Log.e(
                                            "Notification",
                                            "Wait failed with error ${Kernel32.INSTANCE.GetLastError()}. Exiting..."
                                        )
                                        done = true
                                    }
                                }
                            }
                        }
                    } finally {
                        Kernel32.INSTANCE.CloseHandle(hEvent)
                    }
                } finally {
                    wtlc.WTLC_Template_Destroy(template)
                }
            } finally {
                wtlc.WTLC_Instance_Destroy(instance)
            }
        } finally {
            Ole32.INSTANCE.CoUninitialize()
        }
    }
}
