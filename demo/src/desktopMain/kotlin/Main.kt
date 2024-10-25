package com.kdroid.composenotification.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.kdroid.composenotification.builder.AppConfig
import com.kdroid.composenotification.builder.NotificationInitializer
import com.kdroid.composenotification.demo.demo.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Compose Native Notification Demo") {
        NotificationInitializer.configure(

            AppConfig(
                appName = "My awesome app",
                iconIcoPath = Res.getUri("drawable/icon.ico"),
                iconPngPath = Res.getUri("drawable/icon.png"),
            )
        )
        App()
    }
}

