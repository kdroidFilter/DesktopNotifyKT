@file:OptIn(ExperimentalResourceApi::class)

package io.github.kdroidfilter.knotify.demo

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
import io.github.kdroidfilter.knotify.builder.ExperimentalNotificationsApi
import io.github.kdroidfilter.knotify.builder.getNotificationProvider
import io.github.kdroidfilter.knotify.builder.notification
import com.kdroid.kmplog.Log
import com.kdroid.kmplog.d
import io.github.kdroidfilter.knotify.demo.demo.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi


@Composable
fun App() {
    Log.setDevelopmentMode(true)

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


@OptIn(ExperimentalResourceApi::class, ExperimentalNotificationsApi::class)
@Composable
fun ScreenOne(onNavigate: () -> Unit, notificationMessage: String?, onShowMessage: (String?) -> Unit) {
    val myNotification = notification(
        title = "Notification from Screen 1",
        message = "This is a test notification from Screen 1",
        largeImage = Res.getUri("drawable/kdroid.png"),
        onActivated = { Log.d("NotificationLog", "Notification 1 activated") },
        onDismissed = { reason -> Log.d("NotificationLog", "Notification 1 dismissed: $reason")},
        onFailed = {Log.d("NotificationLog", "Notification 1 failed")}
    ) {
        button(title = "Show Message from Button 1") {
            Log.d("NotificationLog", "Button 1 from Screen 1 clicked")
            onShowMessage("Button 1 clicked from Screen 1's notification")
        }
        button(title = "Hide Message from Button 2") {
            Log.d("NotificationLog", "Button 2 from Screen 1 clicked")
            onShowMessage("Button 2 clicked from Screen 1's notification")
        }
    }
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
                 myNotification.send()
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Send notification from Screen 1")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // hide it
                myNotification.hide()
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Hide notification from Screen 1")
        }

        notificationMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@OptIn(ExperimentalNotificationsApi::class)
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


        // Store the notification in a remember variable so we can reference it later
        val myNotification = remember {
            notification(
                largeImage = Res.getUri("drawable/compose.png"),
                title = "Notification from Screen 2",
                message = "This is a test notification from Screen 2",
                onActivated = { Log.d("NotificationLog", "Notification activated") },
                onDismissed = { reason -> Log.d("NotificationLog", "Notification dismissed: $reason")},
                onFailed = {Log.d("NotificationLog", "Notification failed")}
            ) {
                button(title = "Show Message from Button 1") {
                    Log.d("NotificationLog", "Button 1 from Screen 2 clicked")
                    onShowMessage("Button 1 clicked from Screen 2's notification")
                }
                button(title = "Hide Message from Button 2") {
                    Log.d("NotificationLog", "Button 2 from Screen 2 clicked")
                    onShowMessage("Button 2 clicked from Screen 2's notification")
                }
            }
        }

        Button(
            onClick = {
                myNotification.send()
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Send notification from Screen 2")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // hide it
                myNotification.hide()
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Hide notification from Screen 2")
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
