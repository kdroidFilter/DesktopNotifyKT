#ifndef NOTIFICATION_LIBRARY_H
#define NOTIFICATION_LIBRARY_H

#include <libnotify/notify.h>
#include <gdk-pixbuf/gdk-pixbuf.h>  // Inclusion de GdkPixbuf

// Type défini pour la notification
typedef NotifyNotification Notification;

// Type défini pour les callbacks d'action
typedef void (*NotifyActionCallback)(NotifyNotification *notification, char *action, gpointer user_data);

// Type défini pour les callbacks de fermeture
typedef void (*NotifyClosedCallback)(NotifyNotification *notification, gpointer user_data);

// Initialise la bibliothèque de notification avec le nom de l'application
int my_notify_init(const char *app_name);

// Crée une nouvelle notification avec une icône
Notification *create_notification(const char *summary, const char *body, const char *icon_path);

// Crée une nouvelle notification avec une image GdkPixbuf
Notification *create_notification_with_pixbuf(const char *summary, const char *body, const char *image_path);

// Ajoute un bouton facultatif à la notification
void add_button_to_notification(Notification *notification, const char *button_id, const char *button_label, NotifyActionCallback callback, gpointer user_data);

// Envoie la notification
int send_notification(Notification *notification);

// Ajoute un callback pour le clic sur la notification
void set_notification_clicked_callback(Notification *notification, NotifyActionCallback callback, gpointer user_data);

// Ajoute un callback pour la fermeture de la notification
void set_notification_closed_callback(Notification *notification, NotifyClosedCallback callback, gpointer user_data);

void set_image_from_pixbuf(Notification *notification, GdkPixbuf *pixbuf);

// Charge un GdkPixbuf à partir d'un fichier
GdkPixbuf *load_pixbuf_from_file(const char *image_path);

// Démarre la boucle principale pour gérer les événements
void run_main_loop();

// Arrête la boucle principale
void quit_main_loop();

// Nettoyage des ressources
void cleanup_notification();



#endif // NOTIFICATION_LIBRARY_H
