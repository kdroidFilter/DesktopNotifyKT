package io.github.kdroidfilter.knotify.demo

import android.R
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.kdroidfilter.knotify.AndroidChannelConfig
import io.github.kdroidfilter.knotify.NotificationInitializer.notificationInitializer
import io.github.kdroidfilter.knotify.demo.App


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationInitializer(
            defaultChannelConfig = AndroidChannelConfig(
                channelId = "Notification Example 1",
                channelName = "Notification Example 1",
                channelDescription = "Notification Example 1",
                channelImportance = NotificationManager.IMPORTANCE_DEFAULT,
                smallIcon = R.drawable.ic_notification_overlay
            )
        )

        setContent {
            App()
        }
    }
}


