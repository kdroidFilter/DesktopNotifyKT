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
import com.kdroid.composenotification.platform.windows.nativeintegration.WinToastLibC
import com.kdroid.composenotification.platform.windows.utils.registerBasicAUMID
import com.kdroid.kmplog.Log
import com.kdroid.kmplog.e
import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.Ole32
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

                val templateType = if (builder.imagePath != null && File(builder.imagePath!!).exists()) {
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

                    if (builder.imagePath != null && File(builder.imagePath!!).exists()) {
                        wtlc.WTLC_Template_setImagePath(template, WString(builder.imagePath))
                    }

                    for (button in builder.buttons) {
                        wtlc.WTLC_Template_addAction(template, WString(button.label))
                    }

                    wtlc.WTLC_Template_setAudioOption(template, WTLC_AudioOption_Constants.Default)
                    wtlc.WTLC_Template_setExpiration(template, 30000) // 30 seconds

                    // Handle callbacks
                    val activatedCallback = object : ToastActivatedCallback {
                        override fun invoke(userData: Pointer?) {
                            builder.onActivated?.invoke()
                        }
                    }

                    val activatedActionCallback = object : ToastActivatedActionCallback {
                        override fun invoke(userData: Pointer?, actionIndex: Int) {
                            builder.buttons.getOrNull(actionIndex)?.onClick?.invoke()
                        }
                    }

                    val dismissedCallback = object : ToastDismissedCallback {
                        override fun invoke(userData: Pointer?, state: Int) {
                            val dismissalReason = when (state) {
                                WTLC_DismissalReason_Constants.UserCanceled -> DismissalReason.UserCanceled
                                WTLC_DismissalReason_Constants.ApplicationHidden -> DismissalReason.ApplicationHidden
                                WTLC_DismissalReason_Constants.TimedOut -> DismissalReason.TimedOut
                                else -> DismissalReason.Unknown
                            }
                            builder.onDismissed?.invoke(dismissalReason)
                        }
                    }

                    val failedCallback = object : ToastFailedCallback {
                        override fun invoke(userData: Pointer?) {
                            builder.onFailed?.invoke()
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
