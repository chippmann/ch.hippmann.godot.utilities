plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.godot.kotlin.jvm)
    id("ch.hippmann.publish")
}

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
    compileOnly(libs.godot.kotlin.jvm.api)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
}

kotlin {
    jvmToolchain(libs.versions.jvmToolchainVersion.get().toInt())
}

java {
    withJavadocJar()
    withSourcesJar()
}

val projectName = name
val baseUrl = "github.com/chippmann/ch.hippmann.godot.utilities"
publishConfig {
    mavenCentralUser = project.propOrEnv("mavenCentralUsername")
    mavenCentralPassword = project.propOrEnv("mavenCentralPassword")
    gpgInMemoryKey = project.propOrEnv("signingInMemoryKey")
    gpgPassword = project.propOrEnv("signingInMemoryKeyPassword")

    pom {
        name.set(projectName)
        description.set("Helpful godot kotlin jvm utilities.")
        url.set("https://$baseUrl")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://$baseUrl/blob/main/LICENSE")
                distribution.set("https://$baseUrl/blob/main/LICENSE")
            }
        }
        developers {
            developer {
                id.set("maintainer")
                name.set("Cedric Hippmann")
                url.set("https://github.com/chippmann")
                email.set("cedric@hippmann.com")
            }
        }
        scm {
            connection.set("scm:git:https://$baseUrl")
            developerConnection.set("scm:git:$baseUrl.git")
            tag.set("main")
            url.set("https://$baseUrl")
        }
    }
}

tasks {
    // disable shadow jar creation to be able to publish (otherwise we have a jar conflict. It's not needed anyways. Ideally this should be fixed in Godot Kotlin directly)
    shadowJar.configure {
        enabled = false
    }
}

fun Project.propOrEnv(name: String): String? {
    var property: String? = findProperty(name) as String?
    if (property == null) {
        property = System.getenv(name)
    }
    return property
}