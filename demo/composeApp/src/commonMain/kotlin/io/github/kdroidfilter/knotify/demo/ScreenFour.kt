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
private val logger = Logger.withTag("CustomSoundDemo")

@OptIn(ExperimentalResourceApi::class, ExperimentalNotificationsApi::class)
@Composable
fun ScreenFour(
    onNavigateBack: () -> Unit,
    notificationMessage: String?,
    onShowMessage: (String?) -> Unit
) {
    // Create a notification with custom sound
    val customSoundNotification = notification(
        title = "Custom Sound Notification",
        message = "This notification plays a custom sound",
        largeIcon = Res.getUri("drawable/kdroid.png"),
        smallIcon = Res.getUri("drawable/compose.png"),
        soundFile = Res.getUri("files/notify-sound-test.mp3"),
        onActivated = { logger.d { "Custom sound notification activated" } },
        onDismissed = { reason -> logger.d { "Custom sound notification dismissed: $reason" } },
        onFailed = { logger.d { "Custom sound notification failed" } }
    ) {
        button(title = "Acknowledge") {
            logger.d { "Acknowledge button clicked" }
            onShowMessage("Acknowledged custom sound notification")
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
            text = "Custom Sound Notification",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 28.sp),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                customSoundNotification.send()
                onShowMessage("Sent notification with custom sound")
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Send Custom Sound Notification")
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                customSoundNotification.hide()
                onShowMessage("Hidden custom sound notification")
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Hide Custom Sound Notification")
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNavigateBack,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Go Back to Screen 1")
        }

        notificationMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}