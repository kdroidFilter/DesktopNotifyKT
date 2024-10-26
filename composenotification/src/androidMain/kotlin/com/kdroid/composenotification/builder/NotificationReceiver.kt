package com.kdroid.composenotification.builder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.kdroid.kmplog.Log
import com.kdroid.kmplog.d

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val buttonId = intent.getIntExtra(EXTRA_BUTTON_ID, -1)

        when (action) {
            ACTION_BUTTON_CLICKED -> {
                val buttonAction = NotificationActionStore.getAction(buttonId)
                buttonAction?.invoke() ?: Log.d("NotificationReceiver", "No action found for button with ID: $buttonId")

                // Annuler la notification après le clic sur le bouton
                val notificationId = 1 // Utilisez l'ID de la notification que vous avez utilisé dans `NotificationHelper`
                NotificationManagerCompat.from(context).cancel(notificationId)
            }
            ACTION_NOTIFICATION_CLICKED -> {
                NotificationActionStore.onNotificationActivated?.invoke()

                // Annuler la notification après le clic sur le bouton
                val notificationId = 1 // Utilisez l'ID de la notification que vous avez utilisé dans `NotificationHelper`
                NotificationManagerCompat.from(context).cancel(notificationId)
            }
            else -> {
                Toast.makeText(context, "Action inconnue", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val ACTION_BUTTON_CLICKED = "com.kdroid.ACTION_BUTTON_CLICKED"
        const val ACTION_NOTIFICATION_CLICKED = "com.kdroid.ACTION_NOTIFICATION_CLICKED"
        const val EXTRA_BUTTON_ID = "extra_button_id"
    }
}


object NotificationActionStore {
    var onNotificationActivated: (() -> Unit)? = null

    private val actionMap = mutableMapOf<Int, () -> Unit>()

    fun addAction(buttonId: Int, action: () -> Unit) {
        actionMap[buttonId] = action
    }

    fun getAction(buttonId: Int): (() -> Unit)? {
        return actionMap[buttonId]
    }

    fun clearActions() {
        actionMap.clear()
    }
}