/**
 * Enum class representing notification duration options.
 */
enum class NotificationDuration {
    SHORT, LONG
}

/**
 * Interface for sending native notifications.
 */
interface Notifier {

    /**
     * Sends a native notification with the specified title, message, application icon, and duration.
     *
     * @param title The title of the notification.
     * @param message The message content of the notification.
     * @param appIcon The path to the application icon to display with the notification.
     *                Note: The `appIcon` parameter is not yet supported on macOS.
     * @param duration The duration for which the notification will be displayed.
     *                 Default is `SHORT`.
     * @return `true` if the notification was successfully sent, `false` otherwise.
     */
    fun notify(
        title: String,
        message: String,
        appIcon: String?,
        duration: NotificationDuration = NotificationDuration.SHORT,
     //   onClick: () -> Unit = {} Todo Add onClick callback
    ): Boolean

}

