/* main.c (exemple d'utilisation) */
#include <stdio.h>
#include "notification_library.h"

// Fonction de callback pour les boutons
void on_custom_button_clicked(NotifyNotification *notification, char *action, gpointer user_data) {
    printf("Button with action '%s' clicked!\n", action);
}

int main() {
    // Création de la notification avec une icône classique
    Notification *notification = create_notification("Hello, World!", "This is a notification with an icon and an image.", "/home/elyahou/Images/images.png");

    if (notification == NULL) {
        return EXIT_FAILURE;
    }

    // Ajout d'une image via GdkPixbuf
    GdkPixbuf *pixbuf = gdk_pixbuf_new_from_file("/home/elyahou/Images/kotel.png", NULL);
    if (pixbuf != NULL) {
        notify_notification_set_image_from_pixbuf(notification, pixbuf);
        g_object_unref(pixbuf);  // Libérer l'image après l'avoir assignée
    } else {
        fprintf(stderr, "Failed to load image: /home/elyahou/Images/kotel.png\n");
    }

    // Ajout des boutons à la notification
    add_button_to_notification(notification, "button1", "Button 1", on_custom_button_clicked, NULL);
    add_button_to_notification(notification, "button2", "Button 2", NULL, NULL);  // Utilisation du callback par défaut

    // Envoi de la notification
    if (send_notification(notification) != EXIT_SUCCESS) {
        return EXIT_FAILURE;
    }

    // Boucle principale pour permettre la gestion des événements de clics
    GMainLoop *loop = g_main_loop_new(NULL, FALSE);
    g_main_loop_run(loop);

    // Nettoyage
    cleanup_notification();

    return EXIT_SUCCESS;
}
