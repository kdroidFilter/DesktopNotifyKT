package com.kdroid.composenotification.utils

import java.io.File
import java.io.InputStream
import java.net.URLDecoder
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.jar.JarFile

fun extractToTempIfDifferent(jarPath: String): File? {
    // Analyse le chemin pour obtenir le chemin du JAR et le chemin de l'entrée
    val jarFilePath = URLDecoder.decode(jarPath.substringAfter("jar:file:").substringBefore("!"), "UTF-8")
    val entryPath = jarPath.substringAfter("!").trimStart('/')

    // Ouvre le JAR à partir du chemin absolu
    JarFile(jarFilePath).use { jarFile ->
        val entry = jarFile.getJarEntry(entryPath) ?: return null

        // Crée un fichier temporaire pour stocker la ressource extraite
        val tempFile = createTempFile("extracted_", ".tmp")

        // Vérifie si le fichier temporaire existe déjà et compare le hash
        if (tempFile.exists()) {
            val tempFileHash = tempFile.sha256()
            jarFile.getInputStream(entry).use { input ->
                val jarEntryHash = input.sha256()
                // Si le hash est identique, pas besoin de le copier à nouveau
                if (tempFileHash == jarEntryHash) {
                    return tempFile
                }
            }
        }

        // Copie le contenu de l'entrée JAR dans le fichier temporaire
        jarFile.getInputStream(entry).use { input ->
            Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        return tempFile
    }
}

// Extension pour calculer le SHA-256 d'un fichier
fun File.sha256(): String = inputStream().use { it.sha256() }

// Extension pour calculer le SHA-256 d'un InputStream
fun InputStream.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(1024)
    var bytesRead: Int
    while (this.read(buffer).also { bytesRead = it } != -1) {
        digest.update(buffer, 0, bytesRead)
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}