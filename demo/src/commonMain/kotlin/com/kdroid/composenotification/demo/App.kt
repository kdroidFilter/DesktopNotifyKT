package com.kdroid.composenotification.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kdroid.composenotification.builder.Notification
import com.kdroid.kmplog.Log
import com.kdroid.kmplog.d
import com.kdroid.kmplog.w
import java.io.File

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.Screen1) }
    MaterialTheme {
        Surface {
            when (currentScreen) {
                Screen.Screen1 -> ScreenOne(onNavigate = { currentScreen = Screen.Screen2 })
                Screen.Screen2 -> ScreenTwo(onNavigate = { currentScreen = Screen.Screen1 })
            }
        }
    }
}

private const val RESOURCE_DIR = "src/jvmTest/resources/"
private const val DEFAULT_APP_ICON = "default_icon.ico"
private const val DEFAULT_LARGE_IMAGE = "default_icon.png"
private const val TAG = "ComposeNotification"
val appIconPath = getAbsolutePath("icon.ico")
val largeImagePath = getAbsolutePath("icon.png")

private fun getAbsolutePath(relativePath: String): String? {
    val file = File(RESOURCE_DIR, relativePath)
    return if (file.exists()) {
        file.absolutePath
    } else {
        Log.w(TAG, "Resource not found: ${file.path}.")
        null
    }
}

@Composable
fun ScreenOne(onNavigate: () -> Unit) {
    Column {
        Text("Écran 1")
        Button(onClick = onNavigate) {
            Text("Aller à l'écran 2")
        }
        Button(onClick = {
            Notification(
                title = "Notification depuis écran 1",
                largeImagePath = largeImagePath,
                message = "Ceci est un test de notification depuis l'écran 1"
            ) {
                Button("Bouton Notification 1") {
                    Log.d("NotificationLog", "Button 1 depuis Ecran 1")
                }
                Button("Bouton Notification 2") {
                    Log.d("NotificationLog", "Button 2 depuis Ecran 1")
                }
                onActivated {
                    Log.d("NotificationLog", "$1")
                }
                onDismissed { reason ->
                    Log.d("NotificationLog", "$1")
                }
                onFailed {
                    Log.d("NotificationLog", "$1")
                }
            }
        }) {
            Text("Envoyer une notification depuis écran 1")
        }
    }
}

@Composable
fun ScreenTwo(onNavigate: () -> Unit) {
    Column {
        Text("Écran 2")
        Button(onClick = onNavigate) {
            Text("Revenir à l'écran 1")
        }
        Button(onClick = {
            Notification(
                largeImagePath = largeImagePath,
                title = "Notification depuis écran 2",
                message = "Ceci est un test de notification depuis l'écran 2"

            ) {
                Button("Bouton Notification 1") {
                    Log.d("NotificationLog", "Button 1 depuis Ecran 2")
                }
                Button("Bouton Notification 2") {
                    Log.d("NotificationLog", "Button 2 depuis Ecran 2")
                }
                onActivated {
                    Log.d("NotificationLog", "$1")
                }
                onDismissed { reason ->
                    Log.d("NotificationLog", "$1")
                }
                onFailed {
                    Log.d("NotificationLog", "$1")
                }
            }
        }) {
            Text("Envoyer une notification depuis écran 2")
        }
    }
}

enum class Screen {
    Screen1,
    Screen2
}
