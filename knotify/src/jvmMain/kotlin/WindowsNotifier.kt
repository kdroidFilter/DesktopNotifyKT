import java.io.File

internal class WindowsNotifier : Notifier {
    override fun notify(title: String, message: String, appIcon: String): Boolean {
        val script = """
            ${'$'}image = '$appIcon'
            ${'$'}bodyText = '$message'
            ${'$'}ToastImageAndText01 = [Windows.UI.Notifications.ToastTemplateType, Windows.UI.Notifications, ContentType = WindowsRuntime]::ToastImageAndText01
            ${'$'}TemplateContent = [Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime]::GetTemplateContent(${'$'}ToastImageAndText01)
            ${'$'}TemplateContent.SelectSingleNode('//image[@id="1"]').SetAttribute('src', ${'$'}image)
            ${'$'}TemplateContent.SelectSingleNode('//text[@id="1"]').InnerText = ${'$'}bodyText
            ${'$'}AppId = '{1AC14E77-02E7-4E5D-B744-2EB1AE5198B7}\WindowsPowerShell\v1.0\powershell.exe'
            [Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier(${'$'}AppId).Show(${'$'}TemplateContent)
        """.trimIndent()

        val tempFile = File.createTempFile("notification", ".ps1")
        tempFile.writeText(script)

        try {
            val process = Runtime.getRuntime().exec("powershell.exe -ExecutionPolicy Bypass -File ${tempFile.absolutePath}")
            val exitCode = process.waitFor()
            return exitCode == 0
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            tempFile.delete()
        }
    }
}