package io.github.kdroidfilter.knotify.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

import io.github.kdroidfilter.knotify.builder.AppConfig
import io.github.kdroidfilter.knotify.builder.NotificationInitializer
import io.github.kdroidfilter.knotify.demo.composeapp.generated.resources.Res
import io.github.kdroidfilter.knotify.demo.composeapp.generated.resources.compose
import io.github.kdroidfilter.knotify.demo.composeapp.generated.resources.kdroid
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Compose Native Notification Demo", icon = painterResource(Res.drawable.compose)) {

        NotificationInitializer.configure(
            AppConfig(
                appName =  window.title,
                smallIcon = Res.getUri("drawable/compose.png"),
            )
        )
        App()
    }
}

