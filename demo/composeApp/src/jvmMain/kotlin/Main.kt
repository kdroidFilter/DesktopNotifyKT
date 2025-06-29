package io.github.kdroidfilter.knotify.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.kdroidfilter.knotify.builder.AppConfig
import io.github.kdroidfilter.knotify.builder.NotificationInitializer
import io.github.kdroidfilter.knotify.demo.composeapp.generated.resources.Res
import io.github.kdroidfilter.knotify.demo.composeapp.generated.resources.compose
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import java.io.File

@OptIn(ExperimentalResourceApi::class)
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Compose Native Notification Demo", icon = painterResource(Res.drawable.compose)) {

        NotificationInitializer.configure(
            AppConfig(
                appName =  window.title,
                smallIcon = Res.getUri("drawable/compose.png"),
            )
        )
        getAppInfo()
        App()
    }
}

fun getAppInfo() {
    val appName = System.getProperty("sun.java.command")?.split(" ")?.first()
    val jarName = System.getProperty("java.class.path")
        ?.split(File.pathSeparator)
        ?.firstOrNull()
        ?.substringAfterLast(File.separator)

    println("App: $appName")
    println("JAR: $jarName")
}