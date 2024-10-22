package com.kdroid.composenotification.builder

import android.app.NotificationManager

data class AndroidChannelConfig(
    val channelId: String = "default",
    val channelName: String = "Default",
    val channelDescription: String = "Default channel",
    val channelImportance: Int = NotificationManager.IMPORTANCE_DEFAULT
)

object NotificationInitializer {
    private var channelConfig: AndroidChannelConfig = AndroidChannelConfig()
    fun configure(config: AndroidChannelConfig) {
        channelConfig = config
    }

    fun getChannelConfig(): AndroidChannelConfig = channelConfig
}
