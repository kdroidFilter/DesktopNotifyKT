package io.github.kdroidfilter.knotify.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
// No longer need to import AppConfig and NotificationInitializer
import io.github.kdroidfilter.knotify.demo.composeapp.generated.resources.Res
import io.github.kdroidfilter.knotify.demo.composeapp.generated.resources.compose
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import java.awt.Frame
import java.io.File
import javax.swing.JFrame

@OptIn(ExperimentalResourceApi::class)
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Compose Native Notification Demo", icon = painterResource(Res.drawable.compose)) {
        App()
    }
}
