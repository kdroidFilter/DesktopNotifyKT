package io.github.kdroidfilter.knotify

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
import io.github.kdroidfilter.knotify.model.Button
import io.github.kdroidfilter.knotify.model.DismissalReason
import java.io.IOException

internal class NotificationManager(private val context: Context) {

    private val config = NotificationInitializer.getChannelConfig()

    init {
        createNotificationChannel()
    }

    /**
     * Creates a notification channel if the Android version supports it (Oreo and above).
     */
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

    /**
     * Sends a notification with the specified title, message, image, and buttons.
     * @param title The title of the notification.
     * @param message The content message of the notification.
     * @param largeImage The path to the large image to be displayed in the notification.
     * @param buttons A list of buttons to be added to the notification.
     * @param onActivated A callback to be invoked when the notification is activated.
     * @param onDismissed A callback to be invoked when the notification is dismissed.
     * @param onFailed A callback to be invoked when sending the notification fails.
     */
    fun sendNotification(
        title: String,
        message: String,
        largeImage: String?,
        buttons: List<Button>,
        onActivated: (() -> Unit)?,
        onDismissed: ((DismissalReason) -> Unit)?,
        onFailed: (() -> Unit)?
    ) {
        val builder = createNotificationBuilder(title, message)

        addClickAction(builder, onActivated)
        addLargeImage(builder, largeImage)
        addButtons(builder, buttons)

        showNotification(builder, onFailed)
    }

    /**
     * Creates a NotificationCompat.Builder with the provided title and message.
     */
    private fun createNotificationBuilder(title: String, message: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, config.channelId)
            .setSmallIcon(config.smallIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    /**
     * Adds a click action to the notification that will trigger a broadcast.
     */
    private fun addClickAction(builder: NotificationCompat.Builder, onActivated: (() -> Unit)?) {
        NotificationActionStore.onNotificationActivated = onActivated

        val clickIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_NOTIFICATION_CLICKED
        }
        val clickPendingIntent = PendingIntent.getBroadcast(
            context, 0, clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(clickPendingIntent)
    }

    /**
     * Adds a large image to the notification if the image path is valid.
     */
    private fun addLargeImage(builder: NotificationCompat.Builder, largeImage: String?) {
        largeImage?.let {
            val cleanedPath = it.replace("file:///android_asset/", "")
            try {
                val inputStream = context.assets.open(cleanedPath)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigLargeIcon(Icon.createWithBitmap(bitmap))  // Setting the large icon to null as we are using big picture
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Adds action buttons to the notification.
     */
    private fun addButtons(builder: NotificationCompat.Builder, buttons: List<Button>) {
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
    }

    /**
     * Displays the notification and handles any failure.
     */
    private fun showNotification(builder: NotificationCompat.Builder, onFailed: (() -> Unit)?) {
        with(NotificationManagerCompat.from(context)) {
            if (areNotificationsEnabled()) {
                try {
                    notify(1, builder.build())
                } catch (e: SecurityException) {
                    onFailed?.invoke()
                }
            }
        }
    }
}
