package com.kdroid.composenotification.builder

import android.app.NotificationManager


data class AndroidChannelConfig(
    val channelId: String = "default",
    val channelName: String = "Default",
    val channelDescription: String = "Default channel",
    val channelImportance: Int = NotificationManager.IMPORTANCE_DEFAULT,
    val smallIcon : Int = android.R.drawable.ic_dialog_info
)

object NotificationInitializer {
    var context: Any? = null
    private var channelConfig: AndroidChannelConfig = AndroidChannelConfig()
    fun configureChannel(config: AndroidChannelConfig) {
        channelConfig = config
    }

    fun getChannelConfig(): AndroidChannelConfig = channelConfig
}
