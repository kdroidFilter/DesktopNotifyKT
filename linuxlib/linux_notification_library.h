#ifndef NOTIFICATION_LIBRARY_H
#define NOTIFICATION_LIBRARY_H

#include <libnotify/notify.h>
#include <gdk-pixbuf/gdk-pixbuf.h>  // Include GdkPixbuf

// Global debug flag
extern int debug_mode;

// Set debug mode (0 = disabled, 1 = enabled)
void set_debug_mode(int enable);

// Type definition for notification
typedef NotifyNotification Notification;

// Type definition for action callbacks
typedef void (*NotifyActionCallback)(NotifyNotification *notification, char *action, gpointer user_data);

// Type definition for closed callbacks
typedef void (*NotifyClosedCallback)(NotifyNotification *notification, gpointer user_data);

// Initialize notification library with application name
int my_notify_init(const char *app_name);

// Create a new notification with an icon
Notification *create_notification(const char *summary, const char *body, const char *icon_path);

// Create a new notification with a GdkPixbuf image
Notification *create_notification_with_pixbuf(const char *summary, const char *body, const char *image_path);

// Add an optional button to the notification
void add_button_to_notification(Notification *notification, const char *button_id, const char *button_label, NotifyActionCallback callback, gpointer user_data);

// Send the notification
int send_notification(Notification *notification);

// Add a callback for notification click
void set_notification_clicked_callback(Notification *notification, NotifyActionCallback callback, gpointer user_data);

// Add a callback for notification close
void set_notification_closed_callback(Notification *notification, NotifyClosedCallback callback, gpointer user_data);

// Set image from GdkPixbuf
void set_image_from_pixbuf(Notification *notification, GdkPixbuf *pixbuf);

// Load a GdkPixbuf from a file
GdkPixbuf *load_pixbuf_from_file(const char *image_path);

// Start the main loop to handle events
void run_main_loop();

// Stop the main loop
void quit_main_loop();

// Clean up resources
void cleanup_notification();

// Close/hide a notification
int close_notification(Notification *notification);



#endif // NOTIFICATION_LIBRARY_H
