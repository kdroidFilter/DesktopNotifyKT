package platform

import Notifier
import java.io.File

internal class WindowsNotifier(private val appName: String) : Notifier {
    override fun notify(title: String, message: String, appIcon: String?): Boolean {
        val script = """
            ${'$'}image = '$appIcon'
            ${'$'}bodyText = '$message'
            ${'$'}titleText = '$title'
            ${'$'}ToastImageAndText02 = [Windows.UI.Notifications.ToastTemplateType, Windows.UI.Notifications, ContentType = WindowsRuntime]::ToastImageAndText02
            ${'$'}TemplateContent = [Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime]::GetTemplateContent(${'$'}ToastImageAndText02)
            ${'$'}TemplateContent.SelectSingleNode('//image[@id="1"]').SetAttribute('src', ${'$'}image)
            ${'$'}TemplateContent.SelectSingleNode('//text[@id="1"]').InnerText = ${'$'}titleText
            ${'$'}TemplateContent.SelectSingleNode('//text[@id="2"]').InnerText = ${'$'}bodyText
            ${'$'}AppId = '$appName'
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

