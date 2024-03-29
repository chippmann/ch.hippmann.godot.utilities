plugins {
    alias(libs.plugins.godot.kotlin.jvm)
    `maven-publish`
}

group = "ch.hippmann.godot"
version = libs.versions.godot.kotlin.jvm.utilities.get()

repositories {
    mavenLocal()
    mavenCentral()
}

godot {
    classPrefix.set("Util")
    projectName.set("utilities")
    isRegistrationFileGenerationEnabled.set(false)
}

dependencies {
    compileOnly(libs.godot.kotlin.jvm)
    implementation(libs.kotlinx.coroutines.core)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        @Suppress("UNUSED_VARIABLE")
        val utilities by creating(MavenPublication::class) {
            pom {
                name.set(project.name)
                description.set("Helpful godot kotlin jvm utilities.")
            }
            artifactId = "utilities"
            description = "Helpful godot kotlin jvm utilities."
            artifact(tasks.jar)
            artifact(tasks.getByName("sourcesJar"))
            artifact(tasks.getByName("javadocJar"))
        }
    }
}