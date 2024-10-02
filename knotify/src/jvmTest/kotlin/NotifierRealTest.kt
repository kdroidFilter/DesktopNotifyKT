import org.junit.jupiter.api.Test
import utils.OsUtils
import kotlin.test.assertTrue

class NotifierRealTest {

    @Test
    fun testSendNotification() {
        val notifier = NotifierFactory.getNotifier("Calculatrice")

        // Sélection de l'icône en fonction du système d'exploitation
        val iconPath = when {
            OsUtils.isWindows() -> "C:\\Windows\\IdentityCRL\\WLive48x48.png"
            OsUtils.isLinux() -> "/usr/share/icons/hicolor/48x48/apps/test-icon.png"
            OsUtils.isMac() -> "/System/Library/CoreServices/CoreTypes.bundle/Contents/Resources/Actions.icns"
            else -> throw UnsupportedOperationException("Operating system not supported for icons")
        }

        val result = notifier.notify(
            "Test Notification",
            "This is a test message from the test suite.",
            iconPath
        )

        // Vérifie que la notification a été envoyée avec succès
        assertTrue(result, "Notification failed to send on this platform.")
    }
}
