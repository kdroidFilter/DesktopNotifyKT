@file:OptIn(ExperimentalResourceApi::class)

package io.github.kdroidfilter.knotify.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.ExperimentalResourceApi
import io.github.kdroidfilter.knotify.demo.ScreenFour

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.Screen1) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                Screen.Screen1 -> ScreenOne(
                    onNavigate = { currentScreen = Screen.Screen2 },
                    onNavigateToComposeDemo = { currentScreen = Screen.Screen3 },
                    notificationMessage = notificationMessage,
                    onShowMessage = { message -> notificationMessage = message },
                    onNavigateToCustomSoundDemo = { currentScreen = Screen.Screen4 }
                )

                Screen.Screen2 -> ScreenTwo(
                    onNavigate = { currentScreen = Screen.Screen1 },
                    onNavigateToComposeDemo = { currentScreen = Screen.Screen3 },
                    notificationMessage = notificationMessage,
                    onShowMessage = { message -> notificationMessage = message }
                )

                Screen.Screen3 -> ScreenThree(
                    onNavigateBack = { currentScreen = Screen.Screen1 },
                    notificationMessage = notificationMessage,
                    onShowMessage = { message -> notificationMessage = message }
                )

                Screen.Screen4 -> ScreenFour(
                    onNavigateBack = { currentScreen = Screen.Screen1 },
                    notificationMessage = notificationMessage,
                    onShowMessage = { message -> notificationMessage = message }
                )
            }
        }
    }
}

enum class Screen {
    Screen1,
    Screen2,
    Screen3,
    Screen4
}
