import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)

}

val appVersion = "1.0.0"
val appPackageName = "io.github.kdroidfilter.knotify.demo"

group = appPackageName
version = appVersion

kotlin {
    jvmToolchain(17)
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":knotify"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.kermit)
            implementation(compose.components.resources)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

    }
}



compose.desktop {
    application {
        mainClass = "io.github.kdroidfilter.knotify.demo.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Dmg)
            packageName = "KnotifyDemo"
            packageVersion = "1.0.0"
            description = "DesktopNotify-KT Sample"
            copyright = "© 2024 KdroidFilter. All rights reserved."

        }

    }
}
