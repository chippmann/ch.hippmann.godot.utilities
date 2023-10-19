plugins {
    id("com.utopia-rise.godot-kotlin-jvm") version "0.7.2-4.1.2"
}

group = "ch.hippmann.godot"
version = "0.0.1"

repositories {
    mavenCentral()
}

godot {
    classPrefix.set("Util")
    projectName.set("utilities")
    isRegistrationFileGenerationEnabled.set(false)
}

dependencies {
    compileOnly("com.utopia-rise:godot-library:0.7.2-4.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

kotlin {
    jvmToolchain(17)
}