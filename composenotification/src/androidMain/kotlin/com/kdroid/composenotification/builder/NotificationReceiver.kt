package com.kdroid.composenotification.builder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.kdroid.kmplog.Log
import com.kdroid.kmplog.d

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        when (action) {
            "ACTION_BUTTON_1" -> Log.d("NotificationReceiver", "Button 1 clicked")
            "ACTION_BUTTON_2" -> Toast.makeText(context, "Bouton 2 appuyé", Toast.LENGTH_SHORT).show()
        }
    }
}