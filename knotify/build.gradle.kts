@file:OptIn(ExperimentalWasmDsl::class)

import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.vannitktech.maven.publish)
}

group = "io.github.kdroidfilter.knotify"
val ref = System.getenv("GITHUB_REF") ?: ""
val version = if (ref.startsWith("refs/tags/")) {
    val tag = ref.removePrefix("refs/tags/")
    if (tag.startsWith("v")) tag.substring(1) else tag
} else "dev"
kotlin {
    jvmToolchain(17)

    jvm()

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


val buildNativeMac: TaskProvider<Exec> = tasks.register<Exec>("buildNativeMac") {
    onlyIf { System.getProperty("os.name").startsWith("Mac") }
    workingDir(rootDir.resolve("maclib"))
    commandLine("./build.sh")
}

val buildNativeWin: TaskProvider<Exec> = tasks.register<Exec>("buildNativeWin") {
    onlyIf { System.getProperty("os.name").startsWith("Windows") }
    workingDir(rootDir.resolve("winlib/WinToastLibC"))
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
        artifactId = "knotify",
        version = version.toString()
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("DesktopNotify-KT")
        description.set("The DesktopNotify-KT is a Kotlin JVM library taht enables developers to add notifications to their desktop applications in a unified way across different platforms, including Windows, macOS and Linux.")
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
                name.set("Elie Gambache")
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
