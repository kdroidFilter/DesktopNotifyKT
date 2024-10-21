package com.kdroid.composenotification.builder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kdroid.composenotification.R

import com.kdroid.composenotification.model.Button
import com.kdroid.composenotification.model.DismissalReason

actual fun getNotificationProvider(): NotificationProvider = AndroidNotificationProvider()

class AndroidNotificationProvider : NotificationProvider {
    override fun sendNotification(builder: NotificationBuilder) {
        val context: Context = this as Context

            // Créer un canal de notification pour les versions Android O et supérieures
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    builder.appName,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Channel for ${builder.appName} notifications"
                }

                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

        // Construire la notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(0)
            .setContentTitle(builder.title)
            .setContentText(builder.message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        builder.appIconPath?.let {
            // Ajouter une icône personnalisée si spécifiée
        }

        builder.largeImagePath?.let {
            // Ajouter une grande image si spécifiée
        }

        // Ajouter les boutons d'action à la notification
        builder.buttons.forEachIndexed { index, button ->
            val actionIntent = /* Créez un PendingIntent pour le bouton */
                notificationBuilder.addAction(
                    0, // Remplacez 0 par l'icône de votre action
                    button.label,
                    actionIntent
                )
        }

        // Afficher la notification
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, notificationBuilder.build())
        }

        // Appeler le callback onActivated
        builder.onActivated?.invoke()
    }

    companion object {
        private const val CHANNEL_ID = "com.kdroid.composenotification.NOTIFICATION_CHANNEL"
        private const val NOTIFICATION_ID = 1
    }
}
