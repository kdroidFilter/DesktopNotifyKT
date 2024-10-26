package com.kdroid.composenotification.builder

import com.kdroid.composenotification.utils.extractToTempIfDifferent

data class AppConfig(
    val appName : String = "Application",
    val smallIcon : String? = null,
    )

object NotificationInitializer {
    private var appConfiguration: AppConfig = AppConfig()

    fun configure(config: AppConfig) {

        val pngPath = config.smallIcon
        val extractedPngPath = pngPath?.let { extractToTempIfDifferent(it)?.absolutePath }


        val newConfig = config.copy(smallIcon = extractedPngPath)
        appConfiguration = newConfig
    }

    fun getAppConfig(): AppConfig = appConfiguration
}
