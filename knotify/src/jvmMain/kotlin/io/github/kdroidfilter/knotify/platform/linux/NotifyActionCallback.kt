package io.github.kdroidfilter.knotify.platform.linux

import com.sun.jna.Callback
import com.sun.jna.Pointer

// Define the callback as a functional interface
@FunctionalInterface
fun interface NotifyActionCallback : Callback {
    fun invoke(notification: Pointer?, action: String?, user_data: Pointer?)
}

// Define the closed callback as a functional interface
@FunctionalInterface
fun interface NotifyClosedCallback : Callback {
    fun invoke(notification: Pointer?, user_data: Pointer?)
}
