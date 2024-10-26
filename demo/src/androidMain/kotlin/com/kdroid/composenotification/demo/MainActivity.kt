package com.kdroid.composenotification.demo

import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kdroid.composenotification.builder.AndroidChannelConfig
import com.kdroid.composenotification.builder.NotificationInitializer.notificationInitializer


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationInitializer(
            defaultChannelConfig = AndroidChannelConfig(
                channelId = "Notification Example 1",
                channelName = "Notification Example 1",
                channelDescription = "Notification Example 1",
                channelImportance = NotificationManager.IMPORTANCE_DEFAULT,
                smallIcon = android.R.drawable.ic_notification_overlay
            )
        )

        setContent {
            App()
        }
    }
}


