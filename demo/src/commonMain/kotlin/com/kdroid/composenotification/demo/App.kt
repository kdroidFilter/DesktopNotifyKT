@file:OptIn(ExperimentalResourceApi::class)

package com.kdroid.composenotification.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kdroid.composenotification.builder.Notification
import com.kdroid.composenotification.builder.getNotificationProvider
import com.kdroid.composenotification.demo.demo.generated.resources.Res
import com.kdroid.kmplog.Log
import com.kdroid.kmplog.d
import org.jetbrains.compose.resources.ExperimentalResourceApi


@Composable
fun App() {
    val notificationProvider = getNotificationProvider()

    val hasPermission by notificationProvider.hasPermissionState
    var currentScreen by remember { mutableStateOf(Screen.Screen1) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (hasPermission) {
                when (currentScreen) {
                    Screen.Screen1 -> ScreenOne(
                        onNavigate = { currentScreen = Screen.Screen2 },
                        notificationMessage = notificationMessage,
                        onShowMessage = { message -> notificationMessage = message }
                    )

                    Screen.Screen2 -> ScreenTwo(
                        onNavigate = { currentScreen = Screen.Screen1 },
                        notificationMessage = notificationMessage,
                        onShowMessage = { message -> notificationMessage = message }
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (permissionDenied) {
                        Text("Permission denied. Please enable notifications in settings.", color = Color.Red)
                    }
                    Button(
                        onClick = {
                            notificationProvider.requestPermission(
                                onGranted = {
                                    notificationProvider.updatePermissionState(true)
                                },
                                onDenied = {
                                    notificationProvider.updatePermissionState(false)
                                    permissionDenied = true
                                }
                            )
                        }
                    ) {
                        Text("Grant permission to show notifications")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalResourceApi::class)
@Composable
fun ScreenOne(onNavigate: () -> Unit, notificationMessage: String?, onShowMessage: (String?) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Screen 1",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 28.sp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNavigate,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Go to Screen 2")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                Notification(
                    title = "Notification from Screen 1",
                    largeImagePath = Res.getUri("drawable/kdroid.png"),
                    message = "This is a test notification from Screen 1",
                    onActivated = { Log.d("NotificationLog", "Notification 1 activated") },
                    onDismissed = { reason -> Log.d("NotificationLog", "Notification 1 dismissed: $reason")},
                    onFailed = {Log.d("NotificationLog", "Notification 1 failed")}
                ) {
                    Button("Show Message from Button 1") {
                        Log.d("NotificationLog", "Button 1 from Screen 1 clicked")
                        onShowMessage("Button 1 clicked from Screen 1's notification")
                    }
                    Button("Hide Message from Button 2") {
                        Log.d("NotificationLog", "Button 2 from Screen 1 clicked")
                        onShowMessage(null)
                    }
                }
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Send notification from Screen 1")
        }

        notificationMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun ScreenTwo(onNavigate: () -> Unit, notificationMessage: String?, onShowMessage: (String?) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Screen 2",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 28.sp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNavigate,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Go back to Screen 1")
        }
        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = {
                Notification(
                    largeImagePath = Res.getUri("drawable/compose.png"),
                    title = "Notification from Screen 2",
                    message = "This is a test notification from Screen 2",
                    onActivated = {    Log.d("NotificationLog", "Notification activated") },
                    onDismissed = { reason -> Log.d("NotificationLog", "Notification dismissed: $reason")},
                    onFailed = {Log.d("NotificationLog", "Notification failed")}
                ) {
                    Button("Show Message from Button 1") {
                        Log.d("NotificationLog", "Button 1 from Screen 2 clicked")
                        onShowMessage("Button 1 clicked from Screen 2's notification")
                    }
                    Button("Hide Message from Button 2") {
                        Log.d("NotificationLog", "Button 2 from Screen 2 clicked")
                        onShowMessage(null)
                    }
                }
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Send notification from Screen 2")
        }

        notificationMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

enum class Screen {
    Screen1,
    Screen2
}
