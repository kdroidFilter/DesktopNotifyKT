package com.kdroid.composenotification.builder

import com.kdroid.composenotification.utils.extractToTempIfDifferent

data class AppConfig(
    val appName : String = "Application",
    val iconIcoPath : String? = null,
    val iconPngPath : String? = null,
    )

object NotificationInitializer {
    private var appConfiguration: AppConfig = AppConfig()

    fun configure(config: AppConfig) {
        val icoPath = config.iconIcoPath
        val extractedIcoPath = icoPath?.let { extractToTempIfDifferent(it)?.absolutePath }

        val pngPath = config.iconPngPath
        val extractedPngPath = pngPath?.let { extractToTempIfDifferent(it)?.absolutePath }


        val newConfig = config.copy(iconIcoPath = extractedIcoPath, iconPngPath = extractedPngPath)
        appConfiguration = newConfig
    }

    fun getAppConfig(): AppConfig = appConfiguration
}
