#ifndef NOTIFICATION_LIBRARY_H
#define NOTIFICATION_LIBRARY_H

#include <libnotify/notify.h>
#include <gdk-pixbuf/gdk-pixbuf.h>  // Inclusion de GdkPixbuf

// Type défini pour la notification
typedef NotifyNotification Notification;

// Crée une nouvelle notification avec une icône
Notification *create_notification(const char *summary, const char *body, const char *icon_path);

// Crée une nouvelle notification avec une image GdkPixbuf
Notification *create_notification_with_pixbuf(const char *summary, const char *body, const char *image_path);

// Ajoute un bouton facultatif à la notification
void add_button_to_notification(Notification *notification, const char *button_id, const char *button_label, NotifyActionCallback callback, gpointer user_data);

// Envoie la notification
int send_notification(Notification *notification);

// Nettoyage des ressources
void cleanup_notification();

#endif // NOTIFICATION_LIBRARY_H
