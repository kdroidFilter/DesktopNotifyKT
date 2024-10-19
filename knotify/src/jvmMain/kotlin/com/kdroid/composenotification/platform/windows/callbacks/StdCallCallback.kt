
import com.sun.jna.Pointer
import com.sun.jna.win32.StdCallLibrary

// Mise à jour des interfaces de callback
interface ToastActivatedCallback : StdCallLibrary.StdCallCallback {
    fun invoke(userData: Pointer?)
}

interface ToastActivatedActionCallback : StdCallLibrary.StdCallCallback {
    fun invoke(userData: Pointer?, actionIndex: Int)
}

interface ToastDismissedCallback : StdCallLibrary.StdCallCallback {
    fun invoke(userData: Pointer?, state: Int)
}

interface ToastFailedCallback : StdCallLibrary.StdCallCallback {
    fun invoke(userData: Pointer?)
}

