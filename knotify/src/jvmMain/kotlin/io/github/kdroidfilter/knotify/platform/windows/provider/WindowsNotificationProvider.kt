package io.github.kdroidfilter.knotify.platform.windows.provider

import io.github.kdroidfilter.knotify.platform.windows.callbacks.ToastActivatedActionCallback
import io.github.kdroidfilter.knotify.platform.windows.callbacks.ToastActivatedCallback
import io.github.kdroidfilter.knotify.platform.windows.callbacks.ToastDismissedCallback
import io.github.kdroidfilter.knotify.platform.windows.callbacks.ToastFailedCallback
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.github.kdroidfilter.knotify.builder.NotificationBuilder
import io.github.kdroidfilter.knotify.builder.NotificationProvider
import io.github.kdroidfilter.knotify.model.DismissalReason

import co.touchlab.kermit.Logger
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.WinBase.WAIT_OBJECT_0
import com.sun.jna.platform.win32.WinError.WAIT_TIMEOUT
import com.sun.jna.ptr.IntByReference
import io.github.kdroidfilter.knotify.builder.AppConfig
import io.github.kdroidfilter.knotify.builder.NotificationInitializer
import io.github.kdroidfilter.knotify.utils.WindowUtils
import io.github.kdroidfilter.knotify.platform.windows.constants.PM_REMOVE
import io.github.kdroidfilter.knotify.platform.windows.constants.QS_ALLEVENTS
import io.github.kdroidfilter.knotify.platform.windows.constants.QS_ALLINPUT
import io.github.kdroidfilter.knotify.platform.windows.constants.WTLC_AudioOption_Constants
import io.github.kdroidfilter.knotify.platform.windows.constants.WTLC_DismissalReason_Constants
import io.github.kdroidfilter.knotify.platform.windows.constants.WTLC_ShortcutPolicy_Constants
import io.github.kdroidfilter.knotify.platform.windows.constants.WTLC_TemplateType_Constants
import io.github.kdroidfilter.knotify.platform.windows.constants.WTLC_TextField_Constants
import io.github.kdroidfilter.knotify.platform.windows.nativeintegration.ExtendedUser32
import io.github.kdroidfilter.knotify.platform.windows.nativeintegration.WinToastLibC
import io.github.kdroidfilter.knotify.platform.windows.utils.registerBasicAUMID
import io.github.kdroidfilter.knotify.utils.extractToTempIfDifferent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


/**
 * WindowsNotificationProvider is a concrete implementation of the NotificationProvider interface
 * used for displaying notifications on Windows operating systems.
 *
 * This class utilizes the WinToast library to display notifications and implements various methods
 * necessary to initialize and display these notifications.
 */
internal class WindowsNotificationProvider : NotificationProvider {

    // No longer using appConfig, using WindowUtils.getWindowsTitle() and builder.smallIconPath instead

    // Initialize Kermit logger
    private val logger = Logger.withTag("WindowsNotificationProvider")

    // Map to store notification IDs returned by Windows Toast API
    private val notificationIds = mutableMapOf<Int, Long>()

    override fun sendNotification(builder: NotificationBuilder) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!initializeCOM()) return@launch

            val wtlc = WinToastLibC.INSTANCE

            if (!checkCompatibility(wtlc)) return@launch

            val instance = createWinToastInstance(wtlc) ?: return@launch

            try {
                if (!configureWinToastInstance(wtlc, instance, builder.smallIconPath)) return@launch

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

    override fun hideNotification(builder: NotificationBuilder) {
        //TODO NOT WORKING YET

        // Get the notification ID from the map
        val notificationId = notificationIds[builder.id] ?: return

        CoroutineScope(Dispatchers.IO).launch {
            if (!initializeCOM()) return@launch

            val wtlc = WinToastLibC.INSTANCE

            if (!checkCompatibility(wtlc)) return@launch

            val instance = createWinToastInstance(wtlc) ?: return@launch

            try {
                if (!configureWinToastInstance(wtlc, instance, builder.smallIconPath)) return@launch

                // Hide the notification
                val hideResult = wtlc.WTLC_hideToast(instance, notificationId)

                if (!hideResult) {
                    logger.e { "Failed to hide notification with ID: $notificationId" }
                } else {
                    // Remove the notification ID from the map
                    notificationIds.remove(builder.id)
                }
            } finally {
                wtlc.WTLC_Instance_Destroy(instance)
                Ole32.INSTANCE.CoUninitialize()
            }
        }
    }


    private suspend fun initializeCOM(): Boolean {
        return withContext(Dispatchers.IO) {
            val hr = Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_APARTMENTTHREADED)
            if (COMUtils.FAILED(hr)) {
                logger.e { "Failed to initialize COM library!" }
                false
            } else {
                true
            }
        }
    }

    private suspend fun checkCompatibility(wtlc: WinToastLibC): Boolean {
        return withContext(Dispatchers.IO) {
            if (!wtlc.WTLC_isCompatible()) {
                logger.e { "Your system is not compatible!" }
                false
            } else {
                true
            }
        }
    }

    private suspend fun createWinToastInstance(wtlc: WinToastLibC): Pointer? {
        return withContext(Dispatchers.IO) {
            val instance = wtlc.WTLC_Instance_Create()
            if (instance == null) {
                logger.e { "Failed to create WinToast instance!" }
            }
            instance
        }
    }

    private suspend fun configureWinToastInstance(
        wtlc: WinToastLibC,
        instance: Pointer,
        smallIconPath: String? = null
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val appName = NotificationInitializer.appConfiguration.appName ?: WindowUtils.getWindowsTitle()
            wtlc.WTLC_setAppName(instance, WString(appName))

            val aumid = "${appName.replace(" ", "")}Toast"
            val registeredAUMID = if (registerBasicAUMID(aumid, appName, smallIconPath ?: NotificationInitializer.appConfiguration.smallIcon.orEmpty())) {
                aumid
            } else {
                logger.w { "Failed to register the AUMID. Using the application name as AUMID." }
                appName
            }

            wtlc.WTLC_setAppUserModelId(instance, WString(registeredAUMID))
            wtlc.WTLC_setShortcutPolicy(instance, WTLC_ShortcutPolicy_Constants.IGNORE)

            val errorRef = IntByReference(0)
            if (!wtlc.WTLC_initialize(instance, errorRef)) {
                val errorMsg = wtlc.WTLC_strerror(errorRef.value).toString()
                logger.e { "Initialization error: $errorMsg" }
                false
            } else {
                true
            }
        }
    }

    private suspend fun createNotificationTemplate(
        wtlc: WinToastLibC,
        builder: NotificationBuilder
    ): Pointer? {
        return withContext(Dispatchers.IO) {
            val templateType = if (builder.largeImagePath != null && File(builder.largeImagePath.toString()).exists()) {
                WTLC_TemplateType_Constants.ImageAndText02
            } else {
                WTLC_TemplateType_Constants.Text02
            }

            val template = wtlc.WTLC_Template_Create(templateType)
            if (template == null) {
                logger.e { "Failed to create the notification template!" }
                return@withContext null
            }

            try {
                wtlc.WTLC_Template_setTextField(template, WString(builder.title), WTLC_TextField_Constants.FirstLine)
                wtlc.WTLC_Template_setTextField(template, WString(builder.message), WTLC_TextField_Constants.SecondLine)

                val largeImagePath = builder.largeImagePath
                val absoluteLargeImagePath = (largeImagePath?.let { extractToTempIfDifferent(it) }?.absolutePath)

                largeImagePath?.let {
                    wtlc.WTLC_Template_setImagePath(template, WString(absoluteLargeImagePath))
                }


                builder.buttons.forEach { button ->
                    wtlc.WTLC_Template_addAction(template, WString(button.label))
                }

                wtlc.WTLC_Template_setAudioOption(template, WTLC_AudioOption_Constants.Default)
                wtlc.WTLC_Template_setExpiration(template, 30000) // 30 seconds

                template
            } catch (e: Exception) {
                logger.e { "Error configuring the template: ${e.message}" }
                wtlc.WTLC_Template_Destroy(template)
                null
            }
        }
    }

    private suspend fun showToast(
        wtlc: WinToastLibC,
        instance: Pointer?,
        template: Pointer?,
        builder: NotificationBuilder
    ) {
        withContext(Dispatchers.IO) {
            if (instance == null || template == null) {
                logger.e { "Instance or template is null" }
                return@withContext
            }

            val errorRef = IntByReference(0)
            val hEvent = Kernel32.INSTANCE.CreateEvent(null, true, false, null)
            if (hEvent == WinBase.INVALID_HANDLE_VALUE) {
                logger.e { "Event creation failed!" }
                return@withContext
            }

            try {
                val callbacks = createCallbacks(hEvent, builder)

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
                    logger.e { "Error displaying the toast: $errorMsg" }
                    return@withContext
                }

                // Store the notification ID for later use
                notificationIds[builder.id] = showResult

                // Start the message loop only if the toast was displayed successfully
                runMessageLoop(hEvent)
            } catch (e: Exception) {
                logger.e { "Unexpected error: ${e.message}" }
            } finally {
                // Always close the event handle, even in case of error
                if (!Kernel32.INSTANCE.CloseHandle(hEvent)) {
                    logger.e { "Unable to close the event handle!" }
                }
            }
        }
    }


    private suspend fun runMessageLoop(hEvent: WinNT.HANDLE) {
        withContext(Dispatchers.IO) {
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
                    logger.w { "Timeout. Exiting the message loop." }
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
                        // Messages in the queue
                        while (user32.PeekMessage(msg, null, 0, 0, PM_REMOVE)) {
                            user32.TranslateMessage(msg)
                            user32.DispatchMessage(msg)
                        }
                    }

                    WAIT_TIMEOUT -> {
                        logger.w { "Timeout exceeded. Exiting the message loop." }
                        isDone = true
                    }

                    else -> {
                        // Error occurred
                        val error = Kernel32.INSTANCE.GetLastError()
                        logger.e { "Failed to wait with error $error. Exiting the message loop." }
                        isDone = true
                    }
                }
            }
        }
    }

    private fun createCallbacks(
        hEvent: WinNT.HANDLE,
        builder: NotificationBuilder
    ): Callbacks {
        val activatedCallback = object : ToastActivatedCallback {
            override fun invoke(userData: Pointer?) {
                builder.onActivated?.invoke()
                Kernel32.INSTANCE.SetEvent(hEvent)
            }
        }

        val activatedActionCallback = object : ToastActivatedActionCallback {
            override fun invoke(userData: Pointer?, actionIndex: Int) {
                builder.buttons.getOrNull(actionIndex)?.onClick?.invoke()
                Kernel32.INSTANCE.SetEvent(hEvent)
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
                Kernel32.INSTANCE.SetEvent(hEvent)
            }
        }

        val failedCallback = object : ToastFailedCallback {
            override fun invoke(userData: Pointer?) {
                builder.onFailed?.invoke()
                Kernel32.INSTANCE.SetEvent(hEvent)
            }
        }

        return Callbacks(
            activatedCallback,
            activatedActionCallback,
            dismissedCallback,
            failedCallback
        )
    }

    private data class Callbacks(
        val activatedCallback: ToastActivatedCallback,
        val activatedActionCallback: ToastActivatedActionCallback,
        val dismissedCallback: ToastDismissedCallback,
        val failedCallback: ToastFailedCallback
    )
}
