package com.kdroid.composenotification.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kdroid.composenotification.builder.AndroidChannelConfig
import com.kdroid.composenotification.builder.NotificationInitializer


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
        NotificationInitializer.context = this
      setContent() {
          App()
      }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

