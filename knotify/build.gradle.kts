plugins {
    alias(libs.plugins.multiplatform)
    id("convention.publication")
}

group = "com.kdroid.knotify"
version = "1.0"

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            //unix like
            implementation ("com.github.hypfvieh:dbus-java:3.3.2")

            //Windows
            implementation("net.java.dev.jna:jna:5.12.1")
            implementation("net.java.dev.jna:jna-platform:5.12.1")

            implementation("org.slf4j:slf4j-api:1.7.32")
            implementation("ch.qos.logback:logback-classic:1.2.11")

        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
            runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
            implementation("io.mockk:mockk:1.12.0")
        }

    }

}

tasks.withType<Test> {
    useJUnitPlatform()
}

