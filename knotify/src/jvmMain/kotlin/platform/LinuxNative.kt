package platform

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

// Définir une interface pour ta bibliothèque
interface NotificationLibrary : Library {
    // Charger la bibliothèque .so
    companion object {
        val INSTANCE: NotificationLibrary = Native.load("notification", NotificationLibrary::class.java)
    }

    // Correspond aux signatures des fonctions C
    fun create_notification(summary: String, body: String, icon_path: String): Pointer?

    fun create_notification_with_pixbuf(summary: String, body: String, image_path: String): Pointer?

    fun add_button_to_notification(notification: Pointer?, button_id: String, button_label: String, callback: NotifyActionCallback?, user_data: Pointer?)

    fun send_notification(notification: Pointer?): Int

    fun set_image_from_pixbuf(notification: Pointer?, pixbuf: Pointer?)

    fun cleanup_notification()

    fun run_main_loop()
}

// Définir le callback comme une interface fonctionnelle
@FunctionalInterface
fun interface NotifyActionCallback : Callback {
    fun invoke(notification: Pointer?, action: String?, user_data: Pointer?)
}

fun main() {
    // Charger la bibliothèque
    val lib = NotificationLibrary.INSTANCE

    // Créer une notification avec une image via GdkPixbuf
    val notification = lib.create_notification_with_pixbuf("Titre de la notification", "Ceci est un message", "/home/elyahou/Images/kotel.png")
    if (notification == null) {
        println("Échec de la création de la notification.")
        return
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

    lib.run_main_loop()
}
