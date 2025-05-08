import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)

}

val appVersion = "1.0.0"
val appPackageName = "io.github.kdroidfilter.knotify.demo"

group = appPackageName
version = appVersion

kotlin {
    jvmToolchain(17)
    androidTarget()
    jvm()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    @OptIn(ExperimentalWasmDsl::class) wasmJs {
        outputModuleName = "composeApp"
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":knotify"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.kmplog)
            implementation(compose.components.resources)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

        androidMain.dependencies {
            implementation(libs.activity.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activity.compose)
        }
        wasmJsMain.dependencies {

        }
    }
}

android {
    namespace = "io.github.kdroidfilter.knotify.demo"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        targetSdk = 35

        applicationId = "io.github.kdroidfilter.knotify.demo"
        versionCode = 1
        versionName = "1.0.0"
    }
}

compose.desktop {
    application {
        mainClass = "io.github.kdroidfilter.knotify.demo.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Dmg)
            packageName = "io.github.kdroidfilter.knotify.sample"
            packageVersion = "1.0.0"
            description = "Compose Native Notification Sample"
            copyright = "© 2024 KdroidFilter. All rights reserved."
        }
    }
}
