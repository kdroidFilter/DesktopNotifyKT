import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class NotifierRealTest {

    @Test
    fun testSendNotification() {
        val notifier = NotifierFactory.getNotifier()

        val result = notifier.notify(
            "Test Notification",
            "This is a test message from the test suite.",
            "C:\\Windows\\IdentityCRL\\WLive48x48.png"
        )

        // Vérifie que la notification a été envoyée avec succès
        assertTrue(result, "Notification failed to send on this platform.")
    }
}
