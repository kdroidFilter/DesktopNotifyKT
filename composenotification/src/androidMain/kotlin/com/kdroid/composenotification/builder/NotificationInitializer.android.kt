package com.kdroid.composenotification.builder

import android.content.Context

actual class NotificationInitializer(private val context: Context) {

    actual fun initialize() {
        // Utiliser le contexte Android pour initialiser des services spécifiques
        // comme les notifications
        println("Initialisation des notifications sous Android avec le contexte.")
    }
}