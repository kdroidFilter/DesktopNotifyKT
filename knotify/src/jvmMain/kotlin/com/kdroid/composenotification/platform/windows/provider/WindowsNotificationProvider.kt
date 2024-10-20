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
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Fournisseur de notifications spécifique à Windows.
 * Ce fournisseur utilise les notifications Toast natives de Windows pour afficher des messages.
 */
internal class WindowsNotificationProvider : NotificationProvider {
    override fun sendNotification(builder: NotificationBuilder) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!initializeCOM()) return@launch

            val wtlc = WinToastLibC.INSTANCE

            if (!checkCompatibility(wtlc)) return@launch

            val instance = createWinToastInstance(wtlc) ?: return@launch

            try {
                if (!configureWinToastInstance(wtlc, instance, builder)) return@launch

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

    private suspend fun initializeCOM(): Boolean {
        return withContext(Dispatchers.IO) {
            val hr = Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_APARTMENTTHREADED)
            if (COMUtils.FAILED(hr)) {
                Log.e("Notification", "Échec de l'initialisation de la bibliothèque COM !")
                false
            } else {
                true
            }
        }
    }

    private suspend fun checkCompatibility(wtlc: WinToastLibC): Boolean {
        return withContext(Dispatchers.IO) {
            if (!wtlc.WTLC_isCompatible()) {
                Log.e("Notification", "Votre système n'est pas compatible !")
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
                Log.e("Notification", "Échec de la création de l'instance WinToast !")
            }
            instance
        }
    }

    private suspend fun configureWinToastInstance(
        wtlc: WinToastLibC,
        instance: Pointer,
        builder: NotificationBuilder
    ): Boolean {
        return withContext(Dispatchers.IO) {
            wtlc.WTLC_setAppName(instance, WString(builder.appName))

            val aumid = "${builder.appName.replace(" ", "")}Toast"
            val registeredAUMID = if (registerBasicAUMID(aumid, builder.appName, builder.appIconPath ?: "")) {
                aumid
            } else {
                Log.w("Notification", "Échec de l'enregistrement de l'AUMID. Utilisation du nom de l'application comme AUMID.")
                builder.appName
            }

            wtlc.WTLC_setAppUserModelId(instance, WString(registeredAUMID))
            wtlc.WTLC_setShortcutPolicy(instance, WTLC_ShortcutPolicy_Constants.IGNORE)

            val errorRef = IntByReference(0)
            if (!wtlc.WTLC_initialize(instance, errorRef)) {
                val errorMsg = wtlc.WTLC_strerror(errorRef.value).toString()
                Log.e("Notification", "Erreur d'initialisation : $errorMsg")
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
                Log.e("Notification", "Échec de la création du modèle de notification !")
                return@withContext null
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
                wtlc.WTLC_Template_setExpiration(template, 30000) // 30 secondes

                template
            } catch (e: Exception) {
                Log.e("Notification", "Erreur lors de la configuration du modèle : ${e.message}")
                wtlc.WTLC_Template_Destroy(template)
                null
            }
        }
    }

    private suspend fun showToast(
        wtlc: WinToastLibC,
        instance: Pointer,
        template: Pointer,
        builder: NotificationBuilder
    ) {
        withContext(Dispatchers.IO) {
            val errorRef = IntByReference(0)
            val hEvent = Kernel32.INSTANCE.CreateEvent(null, true, false, null)
            if (hEvent == WinBase.INVALID_HANDLE_VALUE) {
                Log.e("Notification", "Échec de la création de l'événement !")
                return@withContext
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
                    Log.e("Notification", "Erreur lors de l'affichage du toast : $errorMsg")
                    return@withContext
                }

                runMessageLoop(hEvent)
            } finally {
                Kernel32.INSTANCE.CloseHandle(hEvent)
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
            val timeout = 31000L // 31 secondes
            var isDone = false

            while (!isDone) {
                val elapsedTime = Kernel32.INSTANCE.GetTickCount() - startTime
                if (elapsedTime >= timeout) {
                    Log.w("Notification", "Délai dépassé. Sortie de la boucle de messages.")
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
                        // Événement signalé
                        isDone = true
                    }
                    WAIT_OBJECT_0 + 1 -> {
                        // Messages dans la file d'attente
                        while (user32.PeekMessage(msg, null, 0, 0, PM_REMOVE)) {
                            user32.TranslateMessage(msg)
                            user32.DispatchMessage(msg)
                        }
                    }
                    WAIT_TIMEOUT -> {
                        Log.w("Notification", "Délai d'attente dépassé. Sortie de la boucle de messages.")
                        isDone = true
                    }
                    else -> {
                        // Erreur survenue
                        val error = Kernel32.INSTANCE.GetLastError()
                        Log.e("Notification", "Échec de l'attente avec l'erreur $error. Sortie de la boucle de messages.")
                        isDone = true
                    }
                }
            }
        }
    }

    private fun createCallbacks(
        wtlc: WinToastLibC,
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
