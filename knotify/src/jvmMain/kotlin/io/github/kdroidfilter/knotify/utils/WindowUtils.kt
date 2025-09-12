package io.github.kdroidfilter.knotify.utils

import javax.swing.JFrame
import java.awt.Frame

/**
 * Utility functions for window-related operations.
 */
object WindowUtils {
    /**
     * Gets the title of the first JFrame window.
     * This is used to determine the application name for notifications.
     *
     * @return The title of the first JFrame window, or "Application" if no JFrame is found.
     */
    fun getWindowsTitle(): String {
        return Frame.getFrames()
            .filterIsInstance<JFrame>()
            .map { it.title }
            .firstOrNull()?.takeIf { it.isNotEmpty() } ?: "Application"
    }
}