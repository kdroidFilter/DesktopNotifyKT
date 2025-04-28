package com.kdroid.composenotification.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

import com.kdroid.composenotification.demo.demo.generated.resources.Res
import io.github.kdroidfilter.knotify.builder.AppConfig
import io.github.kdroidfilter.knotify.builder.NotificationInitializer
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Compose Native Notification Demo") {
        NotificationInitializer.configure(
            AppConfig(
                appName = "My awesome app",
                smallIcon = Res.getUri("drawable/kdroid.png"),
            )
        )
        App()
    }
}

