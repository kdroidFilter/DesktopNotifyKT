import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.vannitktech.maven.publish)
}

group = "com.kdroid.composenotification"
version = "0.2.0"

kotlin {
    jvmToolchain(17)
    androidTarget {
        publishLibraryVariants("release")
    }

    jvm()

    js {
        browser {
            webpackTask {
                mainOutputFileName = "shared.js"
            }
        }
        binaries.executable()
    }

    wasmJs {
        browser()
        binaries.executable()
    }



    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kmplog)
            implementation(libs.runtime)

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

    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compilerOptions.options.freeCompilerArgs.add("-Xexport-kdoc")
    }

}

android {
    namespace = "com.kdroid.composenotification"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testOptions.targetSdk = libs.versions.android.targetSdk.get().toInt()
    }
}


mavenPublishing {
    coordinates(
        groupId = "io.github.kdroidfilter",
        artifactId = "composenotification",
        version = version.toString()
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("Compose Native Notification")
        description.set("Compose Native Notification is a Kotlin library for sending native notifications across different operating systems. It provides a simple, unified interface for sending notifications on Linux, Windows, and macOS.")
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

