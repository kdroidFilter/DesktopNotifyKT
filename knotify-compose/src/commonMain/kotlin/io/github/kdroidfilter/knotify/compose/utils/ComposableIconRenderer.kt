package io.github.kdroidfilter.knotify.compose.utils

import androidx.compose.runtime.Composable

/**
 * Interface for rendering Composable content to image files.
 */
expect object ComposableIconRenderer {
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
    ): String
}