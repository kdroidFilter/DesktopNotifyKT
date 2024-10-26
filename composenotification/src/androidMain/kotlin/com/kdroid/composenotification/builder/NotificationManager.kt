package com.kdroid.composenotification.builder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kdroid.composenotification.model.Button
import com.kdroid.composenotification.model.DismissalReason
import java.io.IOException

class NotificationManager(private val context: Context) {

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
        largeImagePath: String?,
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

        // Stocker la fonction onActivated
        NotificationActionStore.onNotificationActivated = onActivated

        // Ajouter un PendingIntent pour le clic de la notification
        val clickIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_NOTIFICATION_CLICKED
        }
        val clickPendingIntent = PendingIntent.getBroadcast(
            context, 0, clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(clickPendingIntent)

        largeImagePath?.let {
            val cleanedPath = it.replace("file:///android_asset/", "")
            try {
                val inputStream = context.assets.open(cleanedPath)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigLargeIcon(Icon.createWithBitmap(bitmap))
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

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
            } catch (e: Exception) {
                onFailed?.invoke()
            }
        }
    }


}