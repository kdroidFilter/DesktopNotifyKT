package io.github.kdroidfilter.knotify.utils

/**
 * Enum representing the runtime mode of the application.
 * DEV: Development mode (running from IDE or gradlew run)
 * DIST: Distribution mode (running from installed package or gradlew runDistributable)
 */
enum class RuntimeMode {
    DEV,
    DIST
}

/**
 * Detects the current runtime mode of the application.
 * 
 * @return RuntimeMode.DEV if running in development mode, RuntimeMode.DIST if running in distribution mode
 */
fun detectRuntimeMode(): RuntimeMode =
    if (System.getProperty("compose.application.resources.dir") == null)
        RuntimeMode.DEV       // gradlew run / IDE
    else
        RuntimeMode.DIST      // gradlew runDistributable ou package installé