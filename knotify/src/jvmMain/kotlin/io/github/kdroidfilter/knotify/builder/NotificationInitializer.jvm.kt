package io.github.kdroidfilter.knotify.builder

import io.github.kdroidfilter.knotify.utils.extractToTempIfDifferent


data class AppConfig(
    val appName: String? = null,
    val smallIcon: String? = null,
)

object NotificationInitializer {
    var appConfiguration: AppConfig = AppConfig()
        private set

    fun configure(config: AppConfig) {
        if (config.appName != null)
            require(config.appName.isNotEmpty()) { "App name must not be empty" }

        val pngPath = config.smallIcon
        val extractedPngPath = pngPath?.let { extractToTempIfDifferent(it)?.absolutePath }


        val newConfig = config.copy(smallIcon = extractedPngPath)
        appConfiguration = newConfig
    }

}
