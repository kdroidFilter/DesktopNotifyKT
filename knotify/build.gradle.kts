plugins {
    alias(libs.plugins.multiplatform)
    id("convention.publication")
    `maven-publish`

}


group = "com.kdroid.knotify"
version = "0.1.1"

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(libs.slf4jApi)
            implementation(libs.logbackClassic)

        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.junitApi)
            runtimeOnly(libs.junitEngine)
            implementation(libs.mockk)
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
            version = "0.1.1"
        }
    }
}
