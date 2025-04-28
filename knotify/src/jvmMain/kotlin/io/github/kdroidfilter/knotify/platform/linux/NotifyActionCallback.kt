package io.github.kdroidfilter.knotify.platform.linux

import com.sun.jna.Callback
import com.sun.jna.Pointer

// Définir le callback comme une interface fonctionnelle
@FunctionalInterface
fun interface NotifyActionCallback : Callback {
    fun invoke(notification: Pointer?, action: String?, user_data: Pointer?)
}


fun interface NotifyClosedCallback : Callback {
    fun invoke(notification: Pointer?, user_data: Pointer?)
}