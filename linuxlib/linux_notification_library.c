#include <libnotify/notify.h>
#include <glib.h>
#include <gdk-pixbuf/gdk-pixbuf.h>
#include <stdlib.h>
#include <stdio.h>
#include "linux_notification_library.h"

/* Global variables */
static GMainLoop *main_loop = NULL;  /* Global declaration of the main loop */
int debug_mode = 0;                  /* Debug mode flag, default is disabled */

/* Set debug mode (0 = disabled, 1 = enabled) */
void set_debug_mode(int enable) {
    debug_mode = enable;
    if (debug_mode) {
        printf("Debug mode enabled\n");
    }
}

/* Debug logging function */
static void debug_log(const char *format, ...) {
    if (debug_mode) {
        va_list args;
        va_start(args, format);
        vprintf(format, args);
        va_end(args);
    }
}

/* Initialize the notification library with the application name */
int my_notify_init(const char *app_name) {
    if (notify_is_initted()) {
        debug_log("Notification system already initialized\n");
        return 1; /* Already initialized */
    }

    int result = notify_init(app_name) ? 1 : 0;
    if (result) {
        debug_log("Notification system initialized with app name: %s\n", app_name);
    } else {
        debug_log("Failed to initialize notification system\n");
    }
    return result;
}

/* Default callback for button click */
static void default_button_clicked_callback(NotifyNotification *notification, char *action, gpointer user_data) {
    debug_log("Button clicked with action: %s\n", action);
    quit_main_loop(); /* Quit the loop when button is clicked */
}

/* Create a notification */
Notification *create_notification(const char *summary, const char *body, const char *icon_path) {
    if (!notify_is_initted()) {
        debug_log("Notification system not initialized, attempting to initialize\n");
        if (!notify_init("Notification Library")) {
            fprintf(stderr, "Failed to initialize notifications.\n");
            return NULL;
        }
    }

    debug_log("Creating notification - Summary: %s, Body: %s, Icon: %s\n", 
              summary, body, icon_path ? icon_path : "none");

    NotifyNotification *notification = notify_notification_new(summary, body, icon_path);
    return notification;
}

/* Create a notification with a GdkPixbuf image */
Notification *create_notification_with_pixbuf(const char *summary, const char *body, const char *image_path) {
    if (!notify_is_initted()) {
        debug_log("Notification system not initialized, attempting to initialize\n");
        if (!notify_init("Notification Library")) {
            fprintf(stderr, "Failed to initialize notifications.\n");
            return NULL;
        }
    }

    debug_log("Creating notification with pixbuf - Summary: %s, Body: %s, Image: %s\n", 
              summary, body, image_path ? image_path : "none");

    NotifyNotification *notification = notify_notification_new(summary, body, NULL);

    /* Load GdkPixbuf image from file */
    GdkPixbuf *pixbuf = gdk_pixbuf_new_from_file(image_path, NULL);
    if (pixbuf != NULL) {
        notify_notification_set_image_from_pixbuf(notification, pixbuf);
        g_object_unref(pixbuf); /* Free the GdkPixbuf after use */
    } else {
        debug_log("Failed to load image: %s\n", image_path);
    }

    return notification;
}

/* Add a button to the notification */
void add_button_to_notification(Notification *notification, const char *button_id, const char *button_label, NotifyActionCallback callback, gpointer user_data) {
    if (notification == NULL) {
        debug_log("Cannot add button: notification is NULL\n");
        return;
    }

    debug_log("Adding button - ID: %s, Label: %s\n", button_id, button_label);

    if (callback == NULL) {
        callback = default_button_clicked_callback;
    }
    notify_notification_add_action(notification, button_id, button_label, callback, user_data, NULL);
}

/* Send the notification */
int send_notification(Notification *notification) {
    if (notification == NULL) {
        debug_log("Cannot send notification: notification is NULL\n");
        return EXIT_FAILURE;
    }

    debug_log("Sending notification\n");

    GError *error = NULL;
    if (!notify_notification_show(notification, &error)) {
        fprintf(stderr, "Failed to send notification: %s\n", error->message);
        g_error_free(error);
        notify_uninit();
        return EXIT_FAILURE;
    }

    debug_log("Notification sent successfully\n");
    return EXIT_SUCCESS;
}

/* Clean up resources */
void cleanup_notification() {
    debug_log("Cleaning up notification resources\n");
    notify_uninit();
}

/* Start the GLib main loop */
void run_main_loop() {
    debug_log("Starting main loop\n");
    if (main_loop == NULL) {
        main_loop = g_main_loop_new(NULL, FALSE);
    }
    g_main_loop_run(main_loop);
}

/* Stop the GLib main loop */
void quit_main_loop() {
    debug_log("Stopping main loop\n");
    if (main_loop != NULL && g_main_loop_is_running(main_loop)) {
        g_main_loop_quit(main_loop);
        g_main_loop_unref(main_loop);
        main_loop = NULL;
    }
}

/* Set image from GdkPixbuf */
void set_image_from_pixbuf(Notification *notification, GdkPixbuf *pixbuf) {
    if (notification == NULL || pixbuf == NULL) {
        debug_log("Cannot set image: notification or pixbuf is NULL\n");
        return;
    }

    debug_log("Setting image from pixbuf\n");
    notify_notification_set_image_from_pixbuf(notification, pixbuf);
}

/* Set callback for notification click */
void set_notification_clicked_callback(Notification *notification, NotifyActionCallback callback, gpointer user_data) {
    if (notification == NULL || callback == NULL) {
        debug_log("Cannot set clicked callback: notification or callback is NULL\n");
        return;
    }

    debug_log("Setting notification clicked callback\n");
    /* Use notify_notification_add_action with "default" as the action ID */
    notify_notification_add_action(notification, "default", "Default", callback, user_data, NULL);
}

/* Set callback for notification close */
void set_notification_closed_callback(Notification *notification, NotifyClosedCallback callback, gpointer user_data) {
    if (notification == NULL) {
        debug_log("Cannot set closed callback: notification is NULL\n");
        return;
    }

    debug_log("Setting notification closed callback\n");
    g_signal_connect(notification, "closed", G_CALLBACK(callback), user_data);
}

/* Load a GdkPixbuf from a file */
GdkPixbuf *load_pixbuf_from_file(const char *image_path) {
    if (image_path == NULL) {
        debug_log("Cannot load pixbuf: image path is NULL\n");
        return NULL;
    }

    debug_log("Loading pixbuf from file: %s\n", image_path);

    GdkPixbuf *pixbuf = gdk_pixbuf_new_from_file(image_path, NULL);
    if (pixbuf == NULL) {
        debug_log("Failed to load image: %s\n", image_path);
    }
    return pixbuf;
}

/* Close/hide a notification */
int close_notification(Notification *notification) {
    if (notification == NULL) {
        debug_log("Cannot close notification: notification is NULL\n");
        return EXIT_FAILURE;
    }

    debug_log("Closing notification\n");

    GError *error = NULL;
    if (!notify_notification_close(notification, &error)) {
        fprintf(stderr, "Failed to close notification: %s\n", error->message);
        g_error_free(error);
        return EXIT_FAILURE;
    }

    debug_log("Notification closed successfully\n");
    return EXIT_SUCCESS;
}

/* Compilation as shared library (libnotification.so) */
/* To compile this library as a .so file (shared library), you can use the following command: */
/* sudo apt install libnotify-dev libglib2.0-dev libgdk-pixbuf2.0-dev */
