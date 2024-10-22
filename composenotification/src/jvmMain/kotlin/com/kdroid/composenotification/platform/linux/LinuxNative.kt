package com.kdroid.composenotification.platform.linux

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

// Définir une interface pour ta bibliothèque
interface LinuxNotificationLibrary : Library {
    companion object {
        val INSTANCE: LinuxNotificationLibrary = Native.load("notification", LinuxNotificationLibrary::class.java)
    }

    fun create_notification(app_name : String, summary: String, body: String, icon_path: String): Pointer?

    fun add_button_to_notification(notification: Pointer?, button_id: String, button_label: String, callback: NotifyActionCallback?, user_data: Pointer?)

    fun send_notification(notification: Pointer?): Int

    fun set_image_from_pixbuf(notification: Pointer?, pixbuf: Pointer?)

    fun cleanup_notification()

    fun run_main_loop()

    fun quit_main_loop()
    // Nouvelle fonction ajoutée pour charger un GdkPixbuf
    fun load_pixbuf_from_file(image_path: String): Pointer?
}


//Sample use
internal fun sendTestNotification() {
    val lib = LinuxNotificationLibrary.INSTANCE

    // Créer une notification simple
    val notification = lib.create_notification(
        "TestApp", // Nom de l'application
        "Titre de la notification", // Titre
        "Ceci est le corps de la notification", // Corps de la notification
        "" // Chemin de l'icône (laisser vide si aucune icône n'est utilisée)
    )

    // Vérifier si la notification a été créée avec succès
    if (notification != null) {
        // Envoyer la notification
        val result = lib.send_notification(notification)

        // Vérifier si l'envoi de la notification a réussi
        if (result == 0) {
            println("Notification envoyée avec succès.")
        } else {
            println("Erreur lors de l'envoi de la notification.")
        }
    } else {
        println("Erreur lors de la création de la notification.")
    }

    // Lancer la boucle principale pour gérer les événements de la notification
    lib.run_main_loop()
}

