import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class NotifierRealTest {

    @Test
    fun testSendNotification() {
        val notifier = NotifierFactory.getNotifier()

        // Appelle la méthode notify directement, peu importe la plateforme
        val result = notifier.notify(
            "Test Notification",
            "This is a test message from the test suite.",
            "" // Icône vide ou un chemin valide vers une icône si nécessaire
        )

        // Vérifie que la notification a été envoyée avec succès
        assertTrue(result, "Notification failed to send on this platform.")
    }
}
