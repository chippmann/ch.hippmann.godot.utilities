
plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

dependencies {
    compileOnly(kotlin("gradle-plugin", version = libs.versions.kotlin.get()))
    implementation(libs.maven.publish)
}

gradlePlugin {
    plugins {
        create("publishPlugin") {
            id = "ch.hippmann.publish"
            displayName = "Gradle plugin for publishing artifacts to maven central"
            implementationClass = "publish.PublishToMavenCentralPlugin"
        }
    }
}
