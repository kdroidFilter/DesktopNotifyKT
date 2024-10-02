import java.io.File

internal class WindowsNotifier : Notifier {
    override fun notify(title: String, message: String, appIcon: String): Boolean {
        val scriptTemplate = """
            [Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] | Out-Null

            ${'$'}template = [Windows.UI.Notifications.ToastNotificationManager]::GetTemplateContent([Windows.UI.Notifications.ToastTemplateType]::ToastImageAndText02)
            ${'$'}toastXml = ${'$'}template

            ${'$'}toastXml.GetElementsByTagName("text")[0].AppendChild(${'$'}toastXml.CreateTextNode("%TITLE%")) | Out-Null
            ${'$'}toastXml.GetElementsByTagName("text")[1].AppendChild(${'$'}toastXml.CreateTextNode("%MESSAGE%")) | Out-Null

            ${'$'}imageNodes = ${'$'}toastXml.GetElementsByTagName("image")
            ${'$'}imageNodes[0].SetAttribute("src", "%ICON%")

            ${'$'}toast = [Windows.UI.Notifications.ToastNotification]::new(${'$'}toastXml)
            ${'$'}notifier = [Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier()
            ${'$'}notifier.Show(${'$'}toast)
        """.trimIndent()

        // Échapper les caractères spéciaux dans le titre et le message
        val escapedTitle = title.replace("\\", "\\\\").replace("\"", "\\\"")
        val escapedMessage = message.replace("\\", "\\\\").replace("\"", "\\\"")

        // Gérer le chemin de l'icône
        val iconUri = if (appIcon.startsWith("http://") || appIcon.startsWith("https://")) {
            appIcon.replace("\"", "\\\"")
        } else {
            val path = File(appIcon).absolutePath.replace("\\", "/")
            "file:///$path".replace("\"", "\\\"")
        }

        val scriptContent = scriptTemplate
            .replace("%TITLE%", escapedTitle)
            .replace("%MESSAGE%", escapedMessage)
            .replace("%ICON%", iconUri)

        return try {
            // Créer un fichier temporaire pour le script PowerShell
            val tempScript = File.createTempFile("notification", ".ps1")
            tempScript.writeText(scriptContent)

            // Exécuter le script PowerShell
            val process = ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-File", tempScript.absolutePath)
                .redirectErrorStream(true)
                .start()

            // Lire la sortie (si nécessaire)
            val output = process.inputStream.bufferedReader().use { it.readText() }

            // Attendre la fin du processus
            val exitCode = process.waitFor()

            // Supprimer le fichier script temporaire
            tempScript.delete()

            exitCode == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
