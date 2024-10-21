package com.kdroid.composenotification.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kdroid.composenotification.builder.AndroidNotificationProvider
import com.kdroid.kmplog.Log
import com.kdroid.kmplog.d


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}