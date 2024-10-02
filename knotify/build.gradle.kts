plugins {
    alias(libs.plugins.multiplatform)
    id("convention.publication")
    `maven-publish`

}


group = "com.kdroid.knotify"
version = "0.1.0"

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            //unix like
            implementation ("com.github.hypfvieh:dbus-java:3.3.2")

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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            groupId = "com.github.kdroidFilter"
            artifactId = "KNotify"
            version = "0.1.0"
        }
    }
}
