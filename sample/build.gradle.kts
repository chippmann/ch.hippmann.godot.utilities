plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.godot.kotlin.jvm)
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(project(":utilities"))
    implementation(libs.kotlinx.coroutines.core)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}