import java.io.IOException

internal class MacNotifier : Notifier {
    override fun notify(title: String, message: String, appIcon: String): Boolean {
        val osaPath = findOsascriptPath() ?: return false

        val script = "display notification \"$message\" with title \"$title\""
        val processBuilder = ProcessBuilder(osaPath, "-e", script)

        return try {
            val process = processBuilder.start()
            process.waitFor() == 0 // Renvoie true si le script a réussi
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        }
    }

    // Méthode pour trouver le chemin de 'osascript'
    private fun findOsascriptPath(): String? {
        return try {
            val process = ProcessBuilder("which", "osascript").start()
            val result = process.inputStream.bufferedReader().readText().trim()
            if (result.isNotEmpty()) result else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
