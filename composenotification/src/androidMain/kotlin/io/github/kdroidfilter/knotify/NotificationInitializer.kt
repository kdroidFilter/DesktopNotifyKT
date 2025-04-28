package io.github.kdroidfilter.knotify

import android.R
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import java.lang.ref.WeakReference


data class AndroidChannelConfig(
    val channelId: String = "default",
    val channelName: String = "Default",
    val channelDescription: String = "Default channel",
    val channelImportance: Int = NotificationManager.IMPORTANCE_DEFAULT,
    val smallIcon : Int = R.drawable.ic_notification_overlay
)

object NotificationInitializer {
    var activityReference: WeakReference<Activity>? = null
    var appContext: Context? = null
    private var channelConfiguration: AndroidChannelConfig = AndroidChannelConfig()

    fun Context.notificationInitializer(defaultChannelConfig: AndroidChannelConfig) {
        appContext = this.applicationContext
        activityReference = WeakReference(this as Activity)
        channelConfiguration = defaultChannelConfig
    }

    fun getChannelConfig(): AndroidChannelConfig = channelConfiguration

}
