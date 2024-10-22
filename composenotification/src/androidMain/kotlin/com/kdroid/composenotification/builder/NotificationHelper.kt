package com.kdroid.composenotification.builder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kdroid.composenotification.model.Button
import com.kdroid.composenotification.model.DismissalReason
import com.kdroid.kmplog.Log
import com.kdroid.kmplog.d
import com.kdroid.kmplog.e

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

    fun sendNotification(
        title: String,
        message: String,
        largeImagePath: Any?,
        buttons: List<Button>,
        onActivated: (() -> Unit)?,
        onDismissed: ((DismissalReason) -> Unit)?,
        onFailed: (() -> Unit)?
    ) {
        val builder = NotificationCompat.Builder(context, config.channelId)
            .setSmallIcon(config.smallIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        buttons.forEachIndexed { index, button ->
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationReceiver.ACTION_BUTTON_CLICKED
                putExtra(NotificationReceiver.EXTRA_BUTTON_ID, index)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, index, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, button.label, pendingIntent)
            NotificationActionStore.addAction(index, button.onClick)
        }

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(1, builder.build())
                onActivated?.invoke()
            } catch (e: Exception) {
                onFailed?.invoke()
            }
        }
    }



}