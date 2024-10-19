package com.kdroid.composenotification.platform.linux

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

// Définir une interface pour ta bibliothèque
interface NotificationLibrary : Library {
    companion object {
        val INSTANCE: NotificationLibrary = Native.load("notification", NotificationLibrary::class.java)
    }

    fun create_notification(summary: String, body: String, icon_path: String): Pointer?

    fun add_button_to_notification(notification: Pointer?, button_id: String, button_label: String, callback: NotifyActionCallback?, user_data: Pointer?)

    fun send_notification(notification: Pointer?): Int

    fun set_image_from_pixbuf(notification: Pointer?, pixbuf: Pointer?)

    fun cleanup_notification()

    fun run_main_loop()

    // Nouvelle fonction ajoutée pour charger un GdkPixbuf
    fun load_pixbuf_from_file(image_path: String): Pointer?
}


// Définir le callback comme une interface fonctionnelle
@FunctionalInterface
fun interface NotifyActionCallback : Callback {
    fun invoke(notification: Pointer?, action: String?, user_data: Pointer?)
}

fun main() {
    // Charger la bibliothèque
    val lib = NotificationLibrary.INSTANCE

    // Créer une notification avec une icône initiale
    val iconPath = "/home/elyahou/Images/images.png"
    val notification = lib.create_notification("Titre de la notification", "Ceci est un message", iconPath)
    if (notification == null) {
        println("Échec de la création de la notification.")
        return
    }

    // Charger l'image GdkPixbuf (kotel.png) et l'ajouter à la notification
    val imagePath = "/home/elyahou/Images/kotel.png"
    val pixbufPointer = lib.load_pixbuf_from_file(imagePath)
    if (pixbufPointer != Pointer.NULL) {
        // Utiliser set_image_from_pixbuf pour assigner l'image kotel.png à la notification
        lib.set_image_from_pixbuf(notification, pixbufPointer)
    } else {
        println("Impossible de charger l'image : $imagePath")
    }

    // Ajouter un bouton avec une action personnalisée
    lib.add_button_to_notification(notification, "btn1", "OK", { _, action, _ ->
        println("Bouton cliqué avec l'action: $action")
    }, Pointer.NULL)

    lib.add_button_to_notification(notification, "btn2", "Annuler", { _, action, _ ->
        println("Bouton cliqué avec l'action: $action")
    }, Pointer.NULL)

    // Envoyer la notification
    val result = lib.send_notification(notification)
    if (result == 0) {
        println("Notification envoyée avec succès.")
    } else {
        println("Échec de l'envoi de la notification.")
    }

    // Boucle principale pour gérer les événements de clics
    lib.run_main_loop()

    // Nettoyage après l'utilisation
    lib.cleanup_notification()
}
