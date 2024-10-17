package platform

import com.sun.jna.*
import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.WinBase.WAIT_OBJECT_0
import com.sun.jna.platform.win32.WinDef.DWORD
import com.sun.jna.platform.win32.WinError.WAIT_TIMEOUT
import com.sun.jna.platform.win32.WinNT.HANDLE
import com.sun.jna.platform.win32.WinUser.MSG
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions
import java.io.File

typealias WTLC_Instance = Pointer
typealias WTLC_Template = Pointer
typealias WTLC_Error = Int
typealias WTLC_DismissalReason = Int
typealias WTLC_TextField = Int
typealias WTLC_TemplateType = Int
typealias WTLC_AudioOption = Int

object WTLC_TextField_Constants {
    const val FirstLine = 0
    const val SecondLine = 1
    const val ThirdLine = 2
}

object WTLC_TemplateType_Constants {
    const val ImageAndText02 = 1
    const val Text02 = 2
}

object WTLC_AudioOption_Constants {
    const val Default = 0
}

object WTLC_DismissalReason_Constants {
    const val UserCanceled = 0
    const val ApplicationHidden = 1
    const val TimedOut = 2
}

object WTLC_ShortcutPolicy_Constants {
    const val IGNORE = 0
}

interface WinToastLibC : Library {
    companion object {
        val INSTANCE: WinToastLibC = Native.load("wintoastlibc", WinToastLibC::class.java)
    }

    fun WTLC_isCompatible(): Boolean
    fun WTLC_Instance_Create(): WTLC_Instance?
    fun WTLC_Instance_Destroy(instance: WTLC_Instance)

    fun WTLC_setAppName(instance: WTLC_Instance, appName: WString)
    fun WTLC_setAppUserModelId(instance: WTLC_Instance, aumi: WString)
    fun WTLC_setShortcutPolicy(instance: WTLC_Instance, policy: Int)

    fun WTLC_initialize(instance: WTLC_Instance, error: IntByReference): Boolean
    fun WTLC_strerror(error: WTLC_Error): WString

    fun WTLC_Template_Create(templateType: WTLC_TemplateType): WTLC_Template?
    fun WTLC_Template_Destroy(template: WTLC_Template)

    fun WTLC_Template_setTextField(template: WTLC_Template, text: WString, field: WTLC_TextField)
    fun WTLC_Template_setAudioOption(template: WTLC_Template, option: WTLC_AudioOption)
    fun WTLC_Template_setExpiration(template: WTLC_Template, milliseconds: Long)
    fun WTLC_Template_setImagePath(template: WTLC_Template, imagePath: WString)
    fun WTLC_Template_addAction(template: WTLC_Template, label: WString)

    fun WTLC_showToast(
        instance: WTLC_Instance,
        template: WTLC_Template,
        userData: Pointer?,
        activatedCallback: ToastActivatedCallback?,
        activatedActionCallback: ToastActivatedActionCallback?,
        dismissedCallback: ToastDismissedCallback?,
        failedCallback: ToastFailedCallback?,
        error: IntByReference
    ): Long
}

interface ToastActivatedCallback : Callback {
    fun invoke(userData: Pointer?)
}

interface ToastActivatedActionCallback : Callback {
    fun invoke(userData: Pointer?, actionIndex: Int)
}

interface ToastDismissedCallback : Callback {
    fun invoke(userData: Pointer?, state: WTLC_DismissalReason)
}

interface ToastFailedCallback : Callback {
    fun invoke(userData: Pointer?)
}

// Étendre l'interface User32 pour inclure MsgWaitForMultipleObjects
// Extend the User32 interface to include missing functions
interface ExtendedUser32 : User32 {
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
        pHandles: Array<HANDLE>,
        bWaitAll: Boolean,
        dwMilliseconds: DWORD,
        dwWakeMask: DWORD
    ): DWORD

}


// Fonction pour enregistrer l'AUMID
fun registerBasicAUMID(aumid: String, displayName: String, iconUri: String): Boolean {
    val rootKeyPath = "Software\\Classes\\AppUserModelId"
    val aumidKeyPath = "$rootKeyPath\\$aumid"

    try {
        // Créer ou ouvrir la clé racine
        Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, rootKeyPath)
        // Créer ou ouvrir la clé AUMID
        Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, aumidKeyPath)
        // Définir la valeur DisplayName
        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, aumidKeyPath, "DisplayName", displayName)
        // Définir la valeur IconUri
        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, aumidKeyPath, "IconUri", iconUri)
        return true
    } catch (e: Exception) {
        println("Exception dans registerBasicAUMID: ${e.message}")
        return false
    }
}

// Constantes manquantes de WinUser
const val PM_REMOVE = 0x0001

const val QS_KEY = 0x0001
const val QS_MOUSEMOVE = 0x0002
const val QS_MOUSEBUTTON = 0x0004
const val QS_POSTMESSAGE = 0x0008
const val QS_TIMER = 0x0010
const val QS_PAINT = 0x0020
const val QS_SENDMESSAGE = 0x0040
const val QS_HOTKEY = 0x0080
const val QS_ALLPOSTMESSAGE = 0x0100
const val QS_RAWINPUT = 0x0400

const val QS_MOUSE = QS_MOUSEMOVE or QS_MOUSEBUTTON
const val QS_INPUT = QS_MOUSE or QS_KEY or QS_RAWINPUT
const val QS_ALLEVENTS = QS_INPUT or QS_POSTMESSAGE or QS_TIMER or QS_PAINT or QS_HOTKEY
const val QS_ALLINPUT = QS_INPUT or QS_POSTMESSAGE or QS_TIMER or QS_PAINT or QS_HOTKEY or QS_SENDMESSAGE


fun main() {
    // Initialiser la bibliothèque COM
    val hr = Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_APARTMENTTHREADED)
    if (COMUtils.FAILED(hr)) {
        println("Échec de l'initialisation de la bibliothèque COM!")
        return
    }

    try {
        val wtlc = WinToastLibC.INSTANCE

        if (!wtlc.WTLC_isCompatible()) {
            println("Votre système n'est pas compatible!")
            return
        }

        val instance = wtlc.WTLC_Instance_Create()
        if (instance == null) {
            println("Échec de la création de l'instance WinToast!")
            return
        }

        try {
            val imagePath = "C:\\ProgramData\\Microsoft\\User Account Pictures\\guest.png"
            val withImage = File(imagePath).exists()

            wtlc.WTLC_setAppName(instance, WString("NotificationExample"))

            val aumid = "ExampleToast"

            if (registerBasicAUMID(aumid, "NotificationExample", imagePath)) {
                wtlc.WTLC_setAppUserModelId(instance, WString(aumid))
            } else {
                wtlc.WTLC_setAppUserModelId(instance, WString("NotificationExample"))
            }

            wtlc.WTLC_setShortcutPolicy(instance, WTLC_ShortcutPolicy_Constants.IGNORE)

            val errorRef = IntByReference(0)
            if (!wtlc.WTLC_initialize(instance, errorRef)) {
                val errorMsg = wtlc.WTLC_strerror(errorRef.value).toString()
                println("Erreur d'initialisation: $errorMsg")
                return
            }

            val templateType = if (withImage) WTLC_TemplateType_Constants.ImageAndText02 else WTLC_TemplateType_Constants.Text02
            val template = wtlc.WTLC_Template_Create(templateType)
            if (template == null) {
                println("Échec de la création du template!")
                return
            }

            try {
                wtlc.WTLC_Template_setTextField(template, WString("Bonjour, le monde!"), WTLC_TextField_Constants.FirstLine)
                wtlc.WTLC_Template_setTextField(template, WString("Ceci est une notification avec des boutons."), WTLC_TextField_Constants.SecondLine)

                if (withImage) {
                    wtlc.WTLC_Template_setImagePath(template, WString(imagePath))
                }

                wtlc.WTLC_Template_addAction(template, WString("Bouton 1"))
                wtlc.WTLC_Template_addAction(template, WString("Bouton 2"))

                wtlc.WTLC_Template_setAudioOption(template, WTLC_AudioOption_Constants.Default)
                wtlc.WTLC_Template_setExpiration(template, 30000)

                // Créer un handle d'événement
                val hEvent = Kernel32.INSTANCE.CreateEvent(null, true, false, null)
                if (hEvent == null || hEvent == WinBase.INVALID_HANDLE_VALUE) {
                    println("Échec de la création de l'événement!")
                    return
                }

                try {
                    // Définir les callbacks à l'intérieur de la fonction main
                    val activatedCallback = object : ToastActivatedCallback {
                        override fun invoke(userData: Pointer?) {
                            println("Toast activé sans action.")
                            Kernel32.INSTANCE.SetEvent(hEvent)
                        }
                    }

                    val activatedActionCallback = object : ToastActivatedActionCallback {
                        override fun invoke(userData: Pointer?, actionIndex: Int) {
                            when (actionIndex) {
                                0 -> println("Bouton 1 cliqué.")
                                1 -> println("Bouton 2 cliqué.")
                                else -> println("Action inconnue cliquée.")
                            }
                            Kernel32.INSTANCE.SetEvent(hEvent)
                        }
                    }

                    val dismissedCallback = object : ToastDismissedCallback {
                        override fun invoke(userData: Pointer?, state: WTLC_DismissalReason) {
                            when (state) {
                                WTLC_DismissalReason_Constants.UserCanceled -> println("Toast fermé par l'utilisateur.")
                                WTLC_DismissalReason_Constants.ApplicationHidden -> println("Toast fermé par l'application.")
                                WTLC_DismissalReason_Constants.TimedOut -> println("Toast expiré.")
                                else -> println("Toast fermé pour une raison inconnue.")
                            }
                            Kernel32.INSTANCE.SetEvent(hEvent)
                        }
                    }

                    val failedCallback = object : ToastFailedCallback {
                        override fun invoke(userData: Pointer?) {
                            println("Échec de l'affichage du toast.")
                            Kernel32.INSTANCE.SetEvent(hEvent)
                        }
                    }

                    // Afficher la notification toast
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
                        println("Erreur lors de l'affichage du toast: $errorMsg")
                    } else {
                        // Implémenter la boucle de messages en utilisant MsgWaitForMultipleObjects
                        val startTime = Kernel32.INSTANCE.GetTickCount()
                        val timeout = 31000L // 31 secondes
                        var done = false

                        val user32 = ExtendedUser32.INSTANCE
                        val msg = MSG()

                        while (!done) {
                            val elapsedTime = Kernel32.INSTANCE.GetTickCount() - startTime
                            if (elapsedTime >= timeout) {
                                println("Délai dépassé. Sortie...")
                                break
                            }
                            val waitTime = timeout - elapsedTime

                            // Utiliser MsgWaitForMultipleObjects pour attendre l'événement ou les messages
                            val waitResult = user32.MsgWaitForMultipleObjects(
                                1,
                                arrayOf<HANDLE>(hEvent),
                                false,
                                DWORD(waitTime),
                                DWORD((QS_ALLEVENTS  or QS_ALLINPUT).toLong())
                            )

                            when (waitResult.toInt()) {
                                WAIT_OBJECT_0.toInt() -> {
                                    // Event signaled
                                    println("Callback called. Exiting...")
                                    done = true
                                }
                                WAIT_OBJECT_0.toInt() + 1 -> {
                                    // Messages are in the queue
                                    while (user32.PeekMessage(msg, null, 0, 0, PM_REMOVE)) {
                                        user32.TranslateMessage(msg)
                                        user32.DispatchMessage(msg)
                                    }
                                }
                                WAIT_TIMEOUT.toInt() -> {
                                    println("Timeout reached. Exiting...")
                                    done = true
                                }
                                else -> {
                                    // An error occurred
                                    println("Wait failed with error ${Kernel32.INSTANCE.GetLastError()}. Exiting...")
                                    done = true
                                }
                            }
                        }
                    }
                } finally {
                    // Fermer le handle d'événement
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
