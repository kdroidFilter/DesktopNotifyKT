package com.kdroid.composenotification.builder

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.NotificationManagerCompat

actual fun getNotificationProvider(): NotificationProvider {
    val androidContext = NotificationInitializer.appContext
    return AndroidNotificationProvider(androidContext as Context)
}

class AndroidNotificationProvider(private val context: Context) : NotificationProvider {

    private val manager = NotificationManager(context)

    override fun sendNotification(builder: NotificationBuilder) {
        // Utilise le NotificationBuilder pour configurer le titre, le message, les actions, etc.
        manager.sendNotification(
            title = builder.title,
            message = builder.message,
            largeImagePath = builder.largeImagePath,
            buttons = builder.buttons,
            onActivated = builder.onActivated,
            onDismissed = builder.onDismissed,
            onFailed = builder.onFailed
        )
    }

    @SuppressLint("ServiceCast")
    override fun hasPermission(): Boolean {
        // Vérification des permissions de notification sur Android
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManagerCompat
        return manager.areNotificationsEnabled()
    }

}
