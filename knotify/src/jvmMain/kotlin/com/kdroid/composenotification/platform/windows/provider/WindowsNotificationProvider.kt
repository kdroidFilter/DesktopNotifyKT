package com.kdroid.composenotification.platform.windows.provider

import ToastActivatedActionCallback
import ToastActivatedCallback
import ToastDismissedCallback
import ToastFailedCallback
import com.kdroid.composenotification.NotificationProvider
import com.kdroid.composenotification.builder.NotificationBuilder
import com.kdroid.composenotification.models.DismissalReason
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
import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.WinBase.WAIT_OBJECT_0
import com.sun.jna.platform.win32.WinError.WAIT_TIMEOUT
import com.sun.jna.ptr.IntByReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * Implementation of the NotificationProvider interface specific to Windows.
 * This provider uses native Windows Toast notifications to display messages.
 */
internal class WindowsNotificationProvider : NotificationProvider {

    override fun sendNotification(builder: NotificationBuilder) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!initializeCOM()) return@launch

            val wtlc = WinToastLibC.INSTANCE

            if (!checkCompatibility(wtlc)) return@launch

            val instance = createWinToastInstance(wtlc) ?: return@launch

            try {
                configureWinToastInstance(wtlc, instance, builder) ?: return@launch

                val template = createNotificationTemplate(wtlc, builder) ?: return@launch

                try {
                    showToast(wtlc, instance, template, builder)
                } finally {
                    wtlc.WTLC_Template_Destroy(template)
                }
            } finally {
                wtlc.WTLC_Instance_Destroy(instance)
                Ole32.INSTANCE.CoUninitialize()
            }
        }
    }

    /**
     * Initializes the COM library.
     */
    private fun initializeCOM(): Boolean {
        val hr = Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_APARTMENTTHREADED)
        if (COMUtils.FAILED(hr)) {
            Log.e("Notification", "Failed to initialize COM library!")
            return false
        }
        return true
    }

    /**
     * Checks if the system is compatible with WinToast.
     */
    private fun checkCompatibility(wtlc: WinToastLibC): Boolean {
        if (!wtlc.WTLC_isCompatible()) {
            Log.e("Notification", "Your system is not compatible!")
            return false
        }
        return true
    }

    /**
     * Creates a WinToast instance.
     */
    private fun createWinToastInstance(wtlc: WinToastLibC): Pointer? {
        val instance = wtlc.WTLC_Instance_Create()
        if (instance == null) {
            Log.e("Notification", "Failed to create WinToast instance!")
        }
        return instance
    }

    /**
     * Configures the WinToast instance with app details.
     */
    private fun configureWinToastInstance(
        wtlc: WinToastLibC,
        instance: Pointer,
        builder: NotificationBuilder
    ): Boolean? {
        wtlc.WTLC_setAppName(instance, WString(builder.appName))

        val aumid = "${builder.appName.replace(" ", "")}Toast"
        val registeredAUMID = if (registerBasicAUMID(aumid, builder.appName, builder.appIconPath ?: "")) {
            aumid
        } else {
            Log.w("Notification", "Failed to register AUMID. Using app name as AUMID.")
            builder.appName
        }

        wtlc.WTLC_setAppUserModelId(instance, WString(registeredAUMID))
        wtlc.WTLC_setShortcutPolicy(instance, WTLC_ShortcutPolicy_Constants.IGNORE)

        val errorRef = IntByReference(0)
        if (!wtlc.WTLC_initialize(instance, errorRef)) {
            val errorMsg = wtlc.WTLC_strerror(errorRef.value).toString()
            Log.e("Notification", "Initialization error: $errorMsg")
            return null
        }
        return true
    }

    /**
     * Creates and configures the notification template.
     */
    private fun createNotificationTemplate(
        wtlc: WinToastLibC,
        builder: NotificationBuilder
    ): Pointer? {
        val templateType = if (builder.largeImagePath != null && File(builder.largeImagePath).exists()) {
            WTLC_TemplateType_Constants.ImageAndText02
        } else {
            WTLC_TemplateType_Constants.Text02
        }

        val template = wtlc.WTLC_Template_Create(templateType)
        if (template == null) {
            Log.e("Notification", "Failed to create template!")
            return null
        }

        try {
            wtlc.WTLC_Template_setTextField(template, WString(builder.title), WTLC_TextField_Constants.FirstLine)
            wtlc.WTLC_Template_setTextField(template, WString(builder.message), WTLC_TextField_Constants.SecondLine)

            builder.largeImagePath?.takeIf { File(it).exists() }?.let {
                wtlc.WTLC_Template_setImagePath(template, WString(it))
            }

            builder.buttons.forEach { button ->
                wtlc.WTLC_Template_addAction(template, WString(button.label))
            }

            wtlc.WTLC_Template_setAudioOption(template, WTLC_AudioOption_Constants.Default)
            wtlc.WTLC_Template_setExpiration(template, 30000) // 30 seconds
        } catch (e: Exception) {
            Log.e("Notification", "Error configuring template: ${e.message}")
            wtlc.WTLC_Template_Destroy(template)
            return null
        }

        return template
    }

    /**
     * Shows the toast notification and handles callbacks.
     */
    private fun showToast(
        wtlc: WinToastLibC,
        instance: Pointer,
        template: Pointer,
        builder: NotificationBuilder
    ) {
        val errorRef = IntByReference(0)
        val hEvent = Kernel32.INSTANCE.CreateEvent(null, true, false, null)
        if (hEvent == WinBase.INVALID_HANDLE_VALUE) {
            Log.e("Notification", "Failed to create event!")
            return
        }

        try {
            val callbacks = createCallbacks(wtlc, hEvent, builder)

            val showResult = wtlc.WTLC_showToast(
                instance,
                template,
                null,
                callbacks.activatedCallback,
                callbacks.activatedActionCallback,
                callbacks.dismissedCallback,
                callbacks.failedCallback,
                errorRef
            )

            if (showResult < 0) {
                val errorMsg = wtlc.WTLC_strerror(errorRef.value).toString()
                Log.e("Notification", "Error showing toast: $errorMsg")
                return
            }

            runMessageLoop(hEvent)
        } finally {
            Kernel32.INSTANCE.CloseHandle(hEvent)
        }
    }

    /**
     * Creates the necessary callback instances for the toast.
     */
    private fun createCallbacks(
        wtlc: WinToastLibC,
        hEvent: WinNT.HANDLE,
        builder: NotificationBuilder
    ): Callbacks {
        val activatedCallback = object : ToastActivatedCallback {
            override fun invoke(userData: Pointer?) {
                try {
                    builder.onActivated?.invoke()
                } catch (e: Exception) {
                    Log.e("Notification", "Error during toast activation: ${e.message}")
                } finally {
                    Kernel32.INSTANCE.SetEvent(hEvent)
                }
            }
        }

        val activatedActionCallback = object : ToastActivatedActionCallback {
            override fun invoke(userData: Pointer?, actionIndex: Int) {
                try {
                    builder.buttons.getOrNull(actionIndex)?.onClick?.invoke()
                } catch (e: Exception) {
                    Log.e("Notification", "Error during toast action activation: ${e.message}")
                } finally {
                    Kernel32.INSTANCE.SetEvent(hEvent)
                }
            }
        }

        val dismissedCallback = object : ToastDismissedCallback {
            override fun invoke(userData: Pointer?, state: Int) {
                try {
                    val dismissalReason = when (state) {
                        WTLC_DismissalReason_Constants.UserCanceled -> DismissalReason.UserCanceled
                        WTLC_DismissalReason_Constants.ApplicationHidden -> DismissalReason.ApplicationHidden
                        WTLC_DismissalReason_Constants.TimedOut -> DismissalReason.TimedOut
                        else -> DismissalReason.Unknown
                    }
                    builder.onDismissed?.invoke(dismissalReason)
                } catch (e: Exception) {
                    Log.e("Notification", "Error during toast dismissal: ${e.message}")
                } finally {
                    Kernel32.INSTANCE.SetEvent(hEvent)
                }
            }
        }

        val failedCallback = object : ToastFailedCallback {
            override fun invoke(userData: Pointer?) {
                try {
                    builder.onFailed?.invoke()
                } catch (e: Exception) {
                    Log.e("Notification", "Error during toast failure: ${e.message}")
                } finally {
                    Kernel32.INSTANCE.SetEvent(hEvent)
                }
            }
        }

        return Callbacks(
            activatedCallback,
            activatedActionCallback,
            dismissedCallback,
            failedCallback
        )
    }

    /**
     * Runs the message loop to handle callbacks and wait for events.
     */
    private fun runMessageLoop(hEvent: WinNT.HANDLE) {
        val user32 = ExtendedUser32.INSTANCE
        val msg = WinUser.MSG()
        val handleArray = Memory(Native.POINTER_SIZE.toLong())
        handleArray.setPointer(0, hEvent.pointer)

        val startTime = Kernel32.INSTANCE.GetTickCount()
        val timeout = 31000L // 31 seconds
        var isDone = false

        while (!isDone) {
            val elapsedTime = Kernel32.INSTANCE.GetTickCount() - startTime
            if (elapsedTime >= timeout) {
                Log.w("Notification", "Timeout. Exiting message loop.")
                break
            }

            val waitTime = (timeout - elapsedTime).toInt()
            val waitResult = user32.MsgWaitForMultipleObjects(
                1,
                handleArray,
                false,
                waitTime,
                QS_ALLEVENTS or QS_ALLINPUT
            )

            when (waitResult) {
                WAIT_OBJECT_0 -> {
                    // Event signaled
                    isDone = true
                }
                WAIT_OBJECT_0 + 1 -> {
                    // Messages in queue
                    while (user32.PeekMessage(msg, null, 0, 0, PM_REMOVE)) {
                        user32.TranslateMessage(msg)
                        user32.DispatchMessage(msg)
                    }
                }
                WAIT_TIMEOUT -> {
                    Log.w("Notification", "Wait timeout. Exiting message loop.")
                    isDone = true
                }
                else -> {
                    // Error occurred
                    val error = Kernel32.INSTANCE.GetLastError()
                    Log.e("Notification", "Wait failed with error $error. Exiting message loop.")
                    isDone = true
                }
            }
        }
    }

    /**
     * Data class to hold callback instances.
     */
    private data class Callbacks(
        val activatedCallback: ToastActivatedCallback,
        val activatedActionCallback: ToastActivatedActionCallback,
        val dismissedCallback: ToastDismissedCallback,
        val failedCallback: ToastFailedCallback
    )
}
