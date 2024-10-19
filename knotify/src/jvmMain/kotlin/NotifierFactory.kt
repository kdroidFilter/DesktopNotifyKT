import com.kdroid.composenotification.platform.LinuxNotifier
import com.kdroid.composenotification.platform.MacNotifier
import com.kdroid.composenotification.platform.WindowsNotifier
import utils.OsUtils

/**
 * Factory object for creating platform-specific notifiers.
 */
object NotifierFactory {

    /**
     * Returns a platform-specific implementation of the [Notifier] interface based on the current operating system.
     *
     * @param appName The name of the application sending the notification.
     * @return An instance of [Notifier] appropriate for the current operating system.
     * @throws UnsupportedOperationException If the operating system is not supported.
     */
    fun getNotifier(appName: String): Notifier {
        return when {
            OsUtils.isLinux() -> LinuxNotifier(appName)
            OsUtils.isWindows() -> WindowsNotifier(appName)
            OsUtils.isMac() -> MacNotifier(appName)
            else -> throw UnsupportedOperationException("Unsupported operating system: ${OsUtils.getOsName()}")
        }
    }
}
