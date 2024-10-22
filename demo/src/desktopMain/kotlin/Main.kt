package com.kdroid.composenotification.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.kdroid.composenotification.builder.AppConfig
import com.kdroid.composenotification.builder.NotificationInitializer

fun main() = application{
    Window(onCloseRequest = ::exitApplication, title = "Compose Native Notification Demo") {
        NotificationInitializer.configure(
            AppConfig(
                appName = "Notification Example 1",
                appIconPath = ""
            )
        )
        App()
    }
}