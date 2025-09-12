package io.github.kdroidfilter.knotify.builder

interface NotificationProvider {

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
    fun hideNotification(builder: NotificationBuilder)


}
