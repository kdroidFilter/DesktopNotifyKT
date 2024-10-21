package com.kdroid.composenotification.builder

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kdroid.composenotification.NotificationActionReceiver
import com.kdroid.composenotification.model.DismissalReason
import com.kdroid.composenotification.model.Button

actual fun getNotificationProvider(context: Any?): NotificationProvider = AndroidNotificationProvider(context = context as Context)

class AndroidNotificationProvider(private val context: Context) : NotificationProvider {

    private var permissionLauncher: ActivityResultLauncher<String>? = null

    companion object {
        private const val CHANNEL_ID = "default_channel_id"
        private const val CHANNEL_NAME = "Default Channel"
        private const val CHANNEL_DESCRIPTION = "A default channel for notifications"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = android.app.NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: android.app.NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun sendNotification(builder: NotificationBuilder) {
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(getAppIcon(builder.appIconPath))
            .setContentTitle(builder.title)
            .setContentText(builder.message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Ajouter une grande image si disponible
        builder.largeImagePath?.let { path ->
            val bitmap = BitmapFactory.decodeFile(path)
            if (bitmap != null) {
                notificationBuilder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)

                )
            }
        }

        // Ajouter les actions (boutons)
        builder.buttons.forEachIndexed { index, button ->
            val actionIntent = createActionIntent(button, builder, index)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                index,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or getPendingIntentFlags()
            )
            notificationBuilder.addAction(0, button.label, pendingIntent)
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getAppIcon(appIconPath: String?): Int {
        return if (appIconPath != null) {
            // Implémentez la logique pour charger une icône depuis un chemin spécifique
            // Ici, nous retournons une icône par défaut pour simplifier
            android.R.drawable.ic_dialog_info
        } else {
            // Icône par défaut de l'application
            context.applicationInfo.icon
        }
    }

    private fun createActionIntent(button: Button, builder: NotificationBuilder, requestCode: Int): Intent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "com.kdroid.composenotification.ACTION_BUTTON_CLICK"
            putExtra("button_label", button.label)
        }
        return intent
    }

    private fun getPendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    override fun hasPermission(): Boolean {
        // Pour Android 13 et plus, vérifiez si l'autorisation de notification est accordée
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }
        // Pour les versions inférieures, vérifiez via NotificationManagerCompat
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        return notificationManagerCompat.areNotificationsEnabled()
    }

    override fun requestPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        if (context is ComponentActivity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Initialiser le launcher une seule fois
                if (permissionLauncher == null) {
                    permissionLauncher = context.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                        if (isGranted) {
                            onGranted()
                        } else {
                            onDenied()
                        }
                    }
                }

                if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    onGranted()
                } else {
                    permissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                // Pour les versions inférieures à Android 13, vérifier si les notifications sont activées
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (notificationManager.areNotificationsEnabled()) {
                    onGranted()
                } else {
                    onDenied()
                }
            }
        } else {
            throw IllegalArgumentException("Le contexte doit être une instance de ComponentActivity pour demander la permission")
        }
    }
}
