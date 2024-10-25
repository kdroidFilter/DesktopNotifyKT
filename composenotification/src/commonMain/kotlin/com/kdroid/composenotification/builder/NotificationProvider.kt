package com.kdroid.composenotification.builder

interface NotificationProvider {
    /**
     * Sends a notification based on the properties and callbacks defined in the [NotificationBuilder].
     *
     * @param builder The builder containing the notification properties and callbacks.
     */
    fun sendNotification(builder: NotificationBuilder)

    /**
     * Checks if the application has permission to display notifications.
     *
     * @return True if the application has notification permission, false otherwise.
     */
    fun hasPermission(): Boolean

    /**
     * Requests permission to display notifications from the user.
     *
     * @param onGranted A callback that is invoked if the permission is granted.
     * @param onDenied A callback that is invoked if the permission is denied.
     */
    fun requestPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        if (hasPermission()) {
            onGranted()
        } else {
            onDenied()
        }
    }
}
