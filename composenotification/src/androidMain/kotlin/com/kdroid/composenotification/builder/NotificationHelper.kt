package com.kdroid.composenotification.builder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val context: Context) {

    private val config = NotificationInitializer.getChannelConfig()

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(config.channelId, config.channelName, config.channelImportance).apply {
                description = config.channelDescription
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification() {
        // Intent pour le bouton 1
        val button1Intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACTION_BUTTON_1"
        }
        val button1PendingIntent: PendingIntent =
            PendingIntent.getBroadcast(context, 0, button1Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Intent pour le bouton 2
        val button2Intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "ACTION_BUTTON_2"
        }
        val button2PendingIntent: PendingIntent =
            PendingIntent.getBroadcast(context, 1, button2Intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Construire la notification
        val builder = NotificationCompat.Builder(context, config.channelId)
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentTitle("Notification Exemple")
            .setContentText("Ceci est une notification avec des boutons.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(0, "Bouton 1", button1PendingIntent)
            .addAction(0, "Bouton 2", button2PendingIntent)


        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
    }
}