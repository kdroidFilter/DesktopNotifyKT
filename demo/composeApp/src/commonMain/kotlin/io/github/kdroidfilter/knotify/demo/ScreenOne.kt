@file:OptIn(ExperimentalResourceApi::class)

package io.github.kdroidfilter.knotify.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kdroidfilter.knotify.builder.ExperimentalNotificationsApi
import io.github.kdroidfilter.knotify.builder.notification
import co.touchlab.kermit.Logger
import io.github.kdroidfilter.knotify.demo.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

// Reference to the shared logger
private val logger = Logger.withTag("NotificationDemo")

@OptIn(ExperimentalResourceApi::class, ExperimentalNotificationsApi::class)
@Composable
fun ScreenOne(
    onNavigate: () -> Unit,
    onNavigateToComposeDemo: () -> Unit,
    notificationMessage: String?,
    onShowMessage: (String?) -> Unit
) {
    val myNotification = notification(
        title = "Notification from Screen 1",
        message = "This is a test notification from Screen 1",
        largeIcon = Res.getUri("drawable/kdroid.png"),
        smallIcon = Res.getUri("drawable/compose.png"),
        onActivated = { logger.d { "Notification 1 activated" } },
        onDismissed = { reason -> logger.d { "Notification 1 dismissed: $reason" } },
        onFailed = { logger.d { "Notification 1 failed" } }
    ) {
        button(title = "Show Message from Button 1") {
            logger.d { "Button 1 from Screen 1 clicked" }
            onShowMessage("Button 1 clicked from Screen 1's notification")
        }
        button(title = "Hide Message from Button 2") {
            logger.d { "Button 2 from Screen 1 clicked" }
            onShowMessage("Button 2 clicked from Screen 1's notification")
        }
    }

    // Create a notification with text input
    val textInputNotification = notification(
        title = "Text Input Notification",
        message = "This notification has a text input field",
        largeIcon = Res.getUri("drawable/kdroid.png"),
        smallIcon = Res.getUri("drawable/compose.png"),
        onActivated = { logger.d { "Text Input notification activated" } },
        onDismissed = { reason -> logger.d { "Text Input notification dismissed: $reason" } },
        onFailed = { logger.d { "Text Input notification failed" } }
    ) {
        textInput(
            id = "text_input_1",
            label = "Reply",
            placeholder = "Type your response here...",
            onTextSubmitted = { text ->
                logger.d { "Text submitted: $text" }
                onShowMessage("You submitted: $text")
            }
        )
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

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                textInputNotification.send()
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Send Text Input Notification")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                textInputNotification.hide()
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Hide Text Input Notification")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNavigateToComposeDemo,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Go to Compose Notification Demo")
        }

        notificationMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
