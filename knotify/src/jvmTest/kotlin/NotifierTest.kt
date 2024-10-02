import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import platform.LinuxNotifier
import platform.MacNotifier
import platform.WindowsNotifier
import utils.OsUtils
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class NotifierTest {

    @BeforeEach
    fun setUp() {
        mockkObject(OsUtils)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testGetNotifierForLinux() {
        every { OsUtils.getOsName() } returns "linux"

        val notifier = NotifierFactory.getNotifier("linux app")
        assertTrue(notifier is LinuxNotifier)
    }

    @Test
    fun testGetNotifierForWindows() {
        every { OsUtils.getOsName() } returns "windows"

        val notifier = NotifierFactory.getNotifier("windows app")
        assertTrue(notifier is WindowsNotifier)
    }

    @Test
    fun testGetNotifierForMac() {
        every { OsUtils.getOsName() } returns "mac"

        val notifier = NotifierFactory.getNotifier("mac app")
        assertTrue(notifier is MacNotifier)
    }

    @Test
    fun testUnsupportedOs() {
        every { OsUtils.getOsName() } returns "unsupportedos"

        assertFailsWith<UnsupportedOperationException> {
            NotifierFactory.getNotifier("unsupported os app")
        }
    }
}
