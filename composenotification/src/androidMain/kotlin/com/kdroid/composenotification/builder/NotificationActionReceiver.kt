package com.kdroid.composenotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.kdroid.composenotification.ACTION_BUTTON_CLICK") {
            val buttonLabel = intent.getStringExtra("button_label")
            // Vous pouvez récupérer et exécuter la fonction `onClick` associée au bouton
            // Cependant, les fonctions ne peuvent pas être passées directement via Intent
            // Vous devrez implémenter une logique pour mapper les labels aux actions
            Toast.makeText(context, "Bouton cliqué : $buttonLabel", Toast.LENGTH_SHORT).show()

            // Fermer la notification
            val notificationManager = NotificationManagerCompat.from(context)
            val NOTIFICATION_ID = 1
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }
}
