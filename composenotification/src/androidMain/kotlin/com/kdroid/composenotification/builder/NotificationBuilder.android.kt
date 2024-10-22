package com.kdroid.composenotification.builder

import android.content.Context
import androidx.core.app.NotificationManagerCompat

actual fun getNotificationProvider(context: Any?): NotificationProvider {
    val androidContext = NotificationInitializer.context
    return AndroidNotificationProvider(androidContext as Context)
}

class AndroidNotificationProvider(private val context: Context) : NotificationProvider {

    private val helper = NotificationManager(context)

    override fun sendNotification(builder: NotificationBuilder) {
        // Utilise le NotificationBuilder pour configurer le titre, le message, les actions, etc.
        helper.sendNotification(
            title = builder.title,
            message = builder.message,
            largeImagePath = builder.largeImagePath,
            buttons = builder.buttons,
            onActivated = builder.onActivated,
            onDismissed = builder.onDismissed,
            onFailed = builder.onFailed
        )
    }

    override fun hasPermission(): Boolean {
        // Vérification des permissions de notification sur Android
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManagerCompat
        return manager.areNotificationsEnabled()
    }

    override fun requestPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        if (hasPermission()) {
            onGranted()
        } else {
            onDenied()
        }
    }
}
