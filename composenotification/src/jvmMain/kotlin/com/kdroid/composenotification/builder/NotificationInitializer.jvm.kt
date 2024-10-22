package com.kdroid.composenotification.builder

data class AppConfig(
    val appName : String = "Application",
    val appIconPath : String? = null,

)

object NotificationInitializer {
    private var appConfiguration: AppConfig = AppConfig()

    fun configure(config: AppConfig) {
        appConfiguration = config
    }

    fun getAppConfig(): AppConfig = appConfiguration
}
