package com.kdroid.composenotification.builder

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat

actual fun getNotificationProvider(context: Any?): NotificationProvider = AndroidNotificationProvider(context = context as Context)

class AndroidNotificationProvider(private val context: Context) : NotificationProvider {

    private var permissionLauncher: ActivityResultLauncher<String>? = null

    override fun sendNotification(builder: NotificationBuilder) {
        TODO("Not yet implemented")
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
