package platform

import Notifier
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

internal class WindowsNotifier(private val appName: String) : Notifier {
    override fun notify(title: String, message: String, appIcon: String?): Boolean {
        println(getAppId(appName))
        val script = """
            ${'$'}image = '$appIcon'
            ${'$'}bodyText = '$message'
            ${'$'}titleText = '$title'
            ${'$'}ToastImageAndText02 = [Windows.UI.Notifications.ToastTemplateType, Windows.UI.Notifications, ContentType = WindowsRuntime]::ToastImageAndText02
            ${'$'}TemplateContent = [Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime]::GetTemplateContent(${'$'}ToastImageAndText02)
            ${'$'}TemplateContent.SelectSingleNode('//image[@id="1"]').SetAttribute('src', ${'$'}image)
            ${'$'}TemplateContent.SelectSingleNode('//text[@id="1"]').InnerText = ${'$'}titleText
            ${'$'}TemplateContent.SelectSingleNode('//text[@id="2"]').InnerText = ${'$'}bodyText
            ${'$'}AppId = '${getAppId(appName) ?: "'{1AC14E77-02E7-4E5D-B744-2EB1AE5198B7}\\WindowsPowerShell\\v1.0\\powershell.exe'"}'
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

    private fun getAppId(appName: String): String? {
        // Commande PowerShell pour rechercher l'application par nom
        val command = listOf("powershell", "-Command", "Get-StartApps | Where-Object {\$_.Name -like '*$appName*'}")

        // Exécution de la commande via ProcessBuilder
        val processBuilder = ProcessBuilder(command)
        val process = processBuilder.start()

        // Lecture de la sortie de la commande
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val errorReader = BufferedReader(InputStreamReader(process.errorStream))

        val output = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            output.append(line).append("\n")
        }

        // Vérifier si une erreur s'est produite
        val error = StringBuilder()
        while (errorReader.readLine().also { line = it } != null) {
            error.append(line).append("\n")
        }

        // Fermeture des flux
        reader.close()
        errorReader.close()

        // Si une erreur a été capturée, la retourner
        if (error.isNotEmpty()) {
            println("Erreur lors de l'exécution de la commande PowerShell : $error")
            return null
        }

        // Recherche de l'application par nom dans la sortie
        val appList = output.lines()
        for (appLine in appList) {
            if (appLine.contains(appName, ignoreCase = true)) {
                // Supposons que l'ID de l'application soit dans une colonne spécifique après le nom
                val columns = appLine.trim().split("\\s+".toRegex())
                if (columns.size > 1) {
                    return columns.last() // L'ID est supposé être dans la dernière colonne
                }
            }
        }

        // Si l'application n'a pas été trouvée
        println("Application $appName non trouvée.")
        return null
    }


}

