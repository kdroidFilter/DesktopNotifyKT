package com.kdroid.composenotification.platform

import com.kdroid.composenotification.NotificationDuration
import com.kdroid.composenotification.Notifier
import com.kdroid.kmplog.Log
import com.kdroid.kmplog.d
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

internal class WindowsNotifier(private val appName: String) : Notifier {
    override fun notify(
        title: String,
        message: String,
        appIcon: String?,
        duration: NotificationDuration,
       // onClick: () -> Unit
    ): Boolean {
        val script = """
            ${'$'}image = '$appIcon'
            ${'$'}bodyText = '$message'
            ${'$'}titleText = '$title'
            ${'$'}ToastImageAndText02 = [Windows.UI.Notifications.ToastTemplateType, Windows.UI.Notifications, ContentType = WindowsRuntime]::ToastImageAndText02
            ${'$'}TemplateContent = [Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime]::GetTemplateContent(${'$'}ToastImageAndText02)
            ${'$'}TemplateContent.SelectSingleNode('//image[@id="1"]').SetAttribute('src', ${'$'}image)
            ${'$'}TemplateContent.SelectSingleNode('//text[@id="1"]').InnerText = ${'$'}titleText
            ${'$'}TemplateContent.SelectSingleNode('//text[@id="2"]').InnerText = ${'$'}bodyText
            ${'$'}AppId = '${getAppId(appName)}'
            [Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier(${'$'}AppId).Show(${'$'}TemplateContent)
        """.trimIndent()

        val tempFile = File.createTempFile("notification", ".ps1")
        tempFile.writeText(script)

        try {
            val processBuilder = ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-File", tempFile.absolutePath)
            processBuilder.redirectErrorStream(true) // Combine stderr and stdout if needed
            val process = processBuilder.start()
            val exitCode = process.waitFor()
            return exitCode == 0
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            tempFile.delete()
        }
    }

    private fun getAppId(appName: String): String? {
        // PowerShell command to find application by name
        val command = listOf("powershell", "-Command", "Get-StartApps | Where-Object {\$_.Name -like '*$appName*'}")

        val processBuilder = ProcessBuilder(command)
        val process = processBuilder.start()

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val errorReader = BufferedReader(InputStreamReader(process.errorStream))

        val output = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            output.append(line).append("\n")
        }

        val error = StringBuilder()
        while (errorReader.readLine().also { line = it } != null) {
            error.append(line).append("\n")
        }

        reader.close()
        errorReader.close()

        if (error.isNotEmpty()) {
            println("Erreur lors de l'exécution de la commande PowerShell : $error")
            return null
        }

        // Search for application by name in output
        val appList = output.lines()
        for (appLine in appList) {
            if (appLine.contains(appName, ignoreCase = true)) {
                val columns = appLine.trim().split("\\s+".toRegex())
                if (columns.size > 1) {
                    println(columns.last())
                    return columns.last()
                }
            }
        }

        // If the application was not found, use powershell id instead
        Log.d("Windows Notifier","Application $appName not found, use powershell id instead, please read https://github.com/kdroidFilter/KNotify?tab=readme-ov-file#important-note-for-windows-users")
        return "{1AC14E77-02E7-4E5D-B744-2EB1AE5198B7}\\WindowsPowerShell\\v1.0\\powershell.exe"
    }

    private fun convertDuration(duration: NotificationDuration) : String {
        return when (duration) {
            NotificationDuration.SHORT -> "default"
            NotificationDuration.LONG -> "long"
        }
    }


}

