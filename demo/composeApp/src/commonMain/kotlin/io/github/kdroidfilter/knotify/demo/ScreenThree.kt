@file:OptIn(ExperimentalResourceApi::class)

package io.github.kdroidfilter.knotify.demo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.Logger
import io.github.kdroidfilter.knotify.builder.ExperimentalNotificationsApi
import io.github.kdroidfilter.knotify.compose.builder.notification
import io.github.kdroidfilter.knotify.demo.composeapp.generated.resources.Res
import io.github.kdroidfilter.knotify.demo.composeapp.generated.resources.compose
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

// Reference to the shared logger
private val logger = Logger.withTag("NotificationDemo")

@OptIn(ExperimentalNotificationsApi::class)
@Composable
fun ScreenThree(
    onNavigateBack: () -> Unit, notificationMessage: String?, onShowMessage: (String?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Compose Notification Demo",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 28.sp),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Preview of the notification logo
        Box(
            modifier = Modifier.size(120.dp).clip(RoundedCornerShape(8.dp))
        ) {
            NotificationLogo()
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "This screen demonstrates using a Composable UI as a notification image",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        val composeNotif = notification(
            title = "Compose Notification",
            message = "This notification uses a Composable UI as its image",
            largeIcon = { NotificationLogo() },
            smallIcon = { NotificationLogo() },
            onActivated = { logger.d { "Compose notification activated" } },
            onDismissed = { reason -> logger.d { "Compose notification dismissed: $reason" } },
            onFailed = { logger.d { "Compose notification failed" } }) {
            button(title = "Show Message") {
                logger.d { "Button clicked from Compose notification" }
                onShowMessage("Button clicked from Compose notification")
            }
        }


        Button(
            onClick = {
                composeNotif.send()
            }, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Send Compose Notification")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                composeNotif.hide()
            }, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Hide Compose Notification")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateBack, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Go back to Screen 1")
        }

        notificationMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

/**
 * A Composable function that creates a simple logo for use in
 * notifications. This demonstrates how to create a custom UI for
 * notifications using Compose.
 */
@Composable
fun NotificationLogo() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(Res.drawable.compose),
            contentDescription = "Notification Logo",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
