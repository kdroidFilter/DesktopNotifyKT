package io.github.kdroidfilter.knotify.utils

import co.touchlab.kermit.Logger
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URI
import java.net.URLDecoder
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.jar.JarFile

// Initialize Kermit logger
private val logger = Logger.withTag("JarResourceExtractor")


fun extractToTempIfDifferent(jarPath: String): File? {
    // Check if the path is a regular file path or a JAR path
    if (!jarPath.startsWith("jar:file:") && !jarPath.contains("!")) {
        // This is a regular file path, not a JAR path
        val file = File(jarPath)
        if (file.exists()) {
            return file
        } else {
            throw FileNotFoundException("File does not exist: $jarPath")
        }
    }

    // Analyze the path to get the file path and the entry path
    val correctedJarFilePath = URLDecoder.decode(jarPath.substringAfter("jar:file:").substringBefore("!"), Charsets.UTF_8.name())

    // Encode special characters to be URI compatible
    val encodedJarFilePath = correctedJarFilePath.replace(" ", "%20")

    // Convert the path to File via URI
    val jarFile = try {
        File(URI("file:" + encodedJarFilePath.replace("\\", "/")))
    } catch (e: IllegalArgumentException) {
        File(correctedJarFilePath.removePrefix("file:"))
    }

    // Check if the file exists
    if (!jarFile.exists()) {
        throw FileNotFoundException("File does not exist: $correctedJarFilePath")
    }

    val entryPath = jarPath.substringAfter("!").trimStart('/')

    // Extract file extension from the original path
    val fileExtension = getFileExtension(jarFile.name)
    logger.d { "Original file extension: $fileExtension" }

    // Logging to verify paths
        logger.d { "Corrected jarFilePath: $correctedJarFilePath" }
        logger.d { "Encoded jarFilePath: $encodedJarFilePath" }
        logger.d { "Entry path: $entryPath" }


    // If the file is not a JAR, handle it differently
    if (!correctedJarFilePath.endsWith(".jar")) {
            logger.d { "The file is not a JAR. Direct copy." }
        val tempFile = createTempFile("extracted_", fileExtension, File(System.getProperty("java.io.tmpdir"))).apply {
            deleteOnExit()
        }

        // Copy the file directly if it is not a JAR
        Files.copy(jarFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        return tempFile
    }

    // Open the JAR from the absolute path
    JarFile(jarFile).use { jar ->
        val entry = jar.getJarEntry(entryPath) ?: return null

        // Extract file extension from the entry name
        val fileExtension = getFileExtension(entryPath)
        logger.d { "JAR entry file extension: $fileExtension" }

        // Create a temporary file to store the extracted resource
        val tempFile = createTempFile("extracted_", fileExtension, File(System.getProperty("java.io.tmpdir"))).apply {
            deleteOnExit()
        }

        // Check if the temporary file already exists and compare the hash
        if (tempFile.exists()) {
            val tempFileHash = tempFile.sha256()
            jar.getInputStream(entry).use { input ->
                val jarEntryHash = input.sha256()
                // If the hash is identical, no need to copy again
                if (tempFileHash == jarEntryHash) {
                    return tempFile
                }
            }
        }

        // Copy the content of the JAR entry to the temporary file
        jar.getInputStream(entry).use { input ->
            Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        return tempFile
    }
}


// Extension to calculate SHA-256 of a file
fun File.sha256(): String = inputStream().use { it.sha256() }

// Extension to calculate SHA-256 of an InputStream
fun InputStream.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(1024)
    var bytesRead: Int
    while (this.read(buffer).also { bytesRead = it } != -1) {
        digest.update(buffer, 0, bytesRead)
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

// Helper function to extract file extension from a path
private fun getFileExtension(path: String): String {
    val lastDotIndex = path.lastIndexOf('.')
    return if (lastDotIndex > 0) {
        // Return the extension with the dot
        path.substring(lastDotIndex)
    } else {
        // Default to .tmp if no extension is found
        ".tmp"
    }
}
