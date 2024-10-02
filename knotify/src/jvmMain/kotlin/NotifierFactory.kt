import platform.LinuxNotifier
import platform.MacNotifier
import platform.WindowsNotifier
import utils.OsUtils

/**
 * Factory object for creating platform-specific notifiers.
 */
object NotifierFactory {

    /**
     * Returns a platform-specific implementation of the [Notifier] interface based on the current operating system.
     *
     * @return An instance of [Notifier] appropriate for the current operating system.
     * @throws UnsupportedOperationException If the operating system is not supported.
     */
    fun getNotifier(): Notifier {
        return when {
            OsUtils.isLinux() -> LinuxNotifier()
            OsUtils.isWindows() -> WindowsNotifier()
            OsUtils.isMac() -> MacNotifier()
            else -> throw UnsupportedOperationException("Unsupported operating system: ${OsUtils.getOsName()}")
        }
    }
}
