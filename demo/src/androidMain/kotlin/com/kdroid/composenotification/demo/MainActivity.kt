package com.kdroid.composenotification.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.kdroid.composenotification.builder.AndroidChannelConfig
import com.kdroid.composenotification.builder.AndroidNotificationProvider
import com.kdroid.composenotification.builder.NotificationHelper
import com.kdroid.composenotification.builder.NotificationInitializer
import com.kdroid.kmplog.Log
import com.kdroid.kmplog.d


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationInitializer.configureChannel(
            AndroidChannelConfig(
                "Notification Example 1",
                channelName = "Notification Example 1",
                channelDescription = "Notification Example 1"
            )
        )
        val notificationProvider = AndroidNotificationProvider(this)
        if (!notificationProvider.hasPermission()) notificationProvider.requestPermission({
            Log.d(
                "NotificationLog",
                "Permission granted")
        }, {
            Log.d(
                "NotificationLog",
                "Permission denied")
        })
        setContent {
            NotificationButton()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

@Composable
fun NotificationButton() {
    val context = LocalContext.current
    val notificationHelper = NotificationHelper(context)

    Button(onClick = {
        notificationHelper.sendNotification()
    }) {
        Text(text = "Envoyer la notification")
    }
}