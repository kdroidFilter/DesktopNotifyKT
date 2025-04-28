package io.github.kdroidfilter.knotify.builder

import androidx.compose.runtime.State

interface NotificationProvider {

    /**
     * A reactive state that represents whether the application has permission to display notifications.
     */
    val hasPermissionState: State<Boolean>

    /**
     * Updates the application's permission state for displaying notifications.
     *
     * @param isGranted A boolean indicating whether the permission was granted (true) or denied (false).
     */
    fun updatePermissionState(isGranted: Boolean) {
    }

    /**
     * Sends a notification based on the properties and callbacks defined in the [NotificationBuilder].
     *
     * @param builder The builder containing the notification properties and callbacks.
     */
    fun sendNotification(builder: NotificationBuilder)

    /**
     * Hides a notification that was previously sent.
     *
     * @param builder The builder containing the notification properties and callbacks.
     */
    fun hideNotification(builder: NotificationBuilder) {
        // Default implementation does nothing
        // Platform-specific implementations should override this method
    }

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
    fun requestPermission(onGranted: () -> Unit = {}, onDenied: () -> Unit = {}) {
        if (hasPermission()) {
            onGranted()
        } else {
            onDenied()
        }
    }
}
