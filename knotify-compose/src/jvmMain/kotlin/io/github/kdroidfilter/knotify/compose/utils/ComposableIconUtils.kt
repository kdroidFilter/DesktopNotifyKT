package io.github.kdroidfilter.knotify.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.use
import kotlinx.coroutines.Dispatchers
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.FilterMipmap
import org.jetbrains.skia.FilterMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.MipmapMode
import java.io.File
import java.util.zip.CRC32
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.use

/**
 * Utility functions for rendering Composable icons to image files for use in system notifications.
 */
object ComposableIconUtils {

    /**
     * Renders a Composable to a PNG file and returns the path to the file.
     *
     * @param iconRenderProperties Properties for rendering the icon
     * @param content The Composable content to render
     * @return Path to the generated PNG file
     */
    fun renderComposableToPngFile(
        iconRenderProperties: IconRenderProperties,
        content: @Composable () -> Unit
    ): String {
        val tempFile = createTempFile(suffix = ".png")
        val pngData = renderComposableToPngBytes(iconRenderProperties, content)
        tempFile.writeBytes(pngData)
        return tempFile.absolutePath
    }

    /**
     * Renders a Composable to a PNG image and returns the result as a byte array.
     *
     * This function creates an [ImageComposeScene] based on the provided [IconRenderProperties],
     * renders the Composable content, and encodes the output into PNG format.
     * If scaling is required based on the [IconRenderProperties], the rendered content is scaled before encoding.
     *
     * @param iconRenderProperties Properties for rendering the icon
     * @param content The Composable content to render
     * @return A byte array containing the rendered PNG image data.
     */
    fun renderComposableToPngBytes(
        iconRenderProperties: IconRenderProperties,
        content: @Composable () -> Unit
    ): ByteArray {
        val scene = ImageComposeScene(
            width = iconRenderProperties.sceneWidth,
            height = iconRenderProperties.sceneHeight,
            density = iconRenderProperties.sceneDensity,
            coroutineContext = Dispatchers.Unconfined
        ) {
            content()
        }

        val renderedIcon = scene.use { it.render() }

        val iconData = if (iconRenderProperties.requiresScaling) {
            val scaledIcon = Bitmap().apply {
                allocN32Pixels(iconRenderProperties.targetWidth, iconRenderProperties.targetHeight)
            }
            renderedIcon.use {
                it.scalePixels(scaledIcon.peekPixels()!!, FilterMipmap(FilterMode.LINEAR, MipmapMode.LINEAR), true)
            }
            scaledIcon.use { bitmap ->
                Image.makeFromBitmap(bitmap).use { image ->
                    image.encodeToData(EncodedImageFormat.PNG)!!
                }
            }
        } else {
            renderedIcon.use { image ->
                image.encodeToData(EncodedImageFormat.PNG)!!
            }
        }

        return iconData.bytes
    }

    /**
     * Creates a temporary file that will be deleted when the JVM exits.
     */
    private fun createTempFile(prefix: String = "notification_icon_", suffix: String): File {
        val tempFile = File.createTempFile(prefix, suffix)
        tempFile.deleteOnExit()
        return tempFile
    }

    /**
     * Calculates a hash value for the rendered composable content.
     * This can be used to detect changes in the composable content without requiring an explicit key.
     *
     * @param iconRenderProperties Properties for rendering the icon
     * @param content The Composable content to render
     * @return A hash value representing the current state of the composable content
     */
    fun calculateContentHash(
        iconRenderProperties: IconRenderProperties,
        content: @Composable () -> Unit
    ): Long {
        // Render the composable to PNG bytes
        val pngBytes = renderComposableToPngBytes(iconRenderProperties, content)

        // Calculate CRC32 hash of the PNG bytes
        val crc = CRC32()
        crc.update(pngBytes)
        return crc.value
    }
}
