@file:OptIn(ExperimentalWasmDsl::class)

import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.vannitktech.maven.publish)
}

group = "io.github.kdroidfilter.knotify"
version = "0.2.0"

kotlin {
    jvmToolchain(17)
    androidTarget {
        publishLibraryVariants("release")
    }

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    wasmJs {
        browser {
            webpackTask {
                mainOutputFileName = "knotifysw.js"
                // Copy knotifysw.js to the output directory
                output.libraryTarget = "umd"
            }
        }
        binaries.executable()
    }



    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kmplog)
            implementation(libs.runtime)
            implementation(libs.platformtools.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.core)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.lifecycle.process)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.jna)
            implementation(libs.jna.platform)
        }
        wasmJsMain.dependencies {
            // import kotlinx datetime
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.browser.wasm.js)
        }

    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compilerOptions.options.freeCompilerArgs.add("-Xexport-kdoc")
    }

}

android {
    namespace = "io.github.kdroidfilter.knotify"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testOptions.targetSdk = libs.versions.android.targetSdk.get().toInt()
    }
}

val buildNativeMac: TaskProvider<Exec> = tasks.register<Exec>("buildNativeMac") {
    onlyIf { System.getProperty("os.name").startsWith("Mac") }
    workingDir(rootDir.resolve("maclib"))
    commandLine("./build.sh")
}

val buildNativeWin: TaskProvider<Exec> = tasks.register<Exec>("buildNativeWin") {
    onlyIf { System.getProperty("os.name").startsWith("Windows") }
    workingDir(rootDir.resolve("winlib"))
    commandLine("cmd", "/c", "build.bat")
}

val buildNativeLinux: TaskProvider<Exec> = tasks.register<Exec>("buildNativeLinux") {
    onlyIf { System.getProperty("os.name").startsWith("Linux") }
    workingDir(rootDir.resolve("linuxlib"))
    commandLine("./build.sh")
}

tasks.register("buildNativeLibraries") {
    dependsOn( buildNativeMac, buildNativeWin, buildNativeLinux)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.kdroidfilter",
        artifactId = "compose-native-notification",
        version = version.toString()
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("K-Notify")
        description.set("The K-Notifylibrary is a Kotlin Multiplatform library designed to work with Compose Multiplatform that enables developers to add notifications to their applications in a unified way across different platforms, including Windows, macOS, Linux, Android, iOS and Web. The main goal of this library is to provide a declarative way to send notifications on Android while removing all boilerplate code, and to enable sending native notifications on other platforms. The library provides seamless integration with Jetpack Compose, allowing notifications to be handled consistently within Compose-based applications.")
        inceptionYear.set("2024")
        url.set("https://github.com/kdroidFilter/ComposeNativeNotification")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        // Specify developers information
        developers {
            developer {
                id.set("kdroidfilter")
                name.set("Elyahou Hadass")
                email.set("elyahou.hadass@gmail.com")
            }
        }

        // Specify SCM information
        scm {
            url.set("https://github.com/kdroidFilter/ComposeNativeNotification")
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)


    // Enable GPG signing for all publications
    signAllPublications()
}


task("testClasses") {}
