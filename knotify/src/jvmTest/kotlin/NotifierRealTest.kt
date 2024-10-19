import com.kdroid.composenotification.NotifierFactory
import org.junit.jupiter.api.Test
import com.kdroid.composenotification.utils.OsUtils
import kotlin.test.assertTrue

class NotifierRealTest {

    @Test
    fun testSendNotification() {
        val notifier = NotifierFactory.getNotifier("My Application")

        // Select the icon according to the operating system
        val iconPath = when {
            OsUtils.isWindows() -> "C:\\Windows\\IdentityCRL\\WLive48x48.png"
            OsUtils.isLinux() -> "/usr/share/icons/hicolor/48x48/apps/zenity.png"
            OsUtils.isMac() -> "/System/Library/CoreServices/CoreTypes.bundle/Contents/Resources/Actions.icns"
            else -> throw UnsupportedOperationException("Operating system not supported for icons")
        }

        val result = notifier.notify(
            "Test Notification",
            "This is a test message from the test suite.",
            iconPath
        )

        // Checks that the notification was sent successfully
        assertTrue(result, "Notification failed to send on this platform.")
    }
}
