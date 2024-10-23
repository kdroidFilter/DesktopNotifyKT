package com.kdroid.composenotification.builder

import android.app.NotificationManager
import android.content.Context


data class AndroidChannelConfig(
    val channelId: String = "default",
    val channelName: String = "Default",
    val channelDescription: String = "Default channel",
    val channelImportance: Int = NotificationManager.IMPORTANCE_DEFAULT,
    val smallIcon : Int = android.R.drawable.ic_dialog_info
)

object NotificationInitializer {
    var appContext: Context? = null
    private var channelConfiguration: AndroidChannelConfig = AndroidChannelConfig()

    fun Context.notificationInitializer(channelConfig: AndroidChannelConfig) {
        appContext = this.applicationContext
        channelConfiguration = channelConfig
    }

    fun getChannelConfig(): AndroidChannelConfig = channelConfiguration

    fun getNotificationManager(): NotificationManager? {
        return appContext?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }
}
