plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.godot.kotlin.jvm)
    id("ch.hippmann.publish")
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

val projectName = name
val baseUrl = "github.com/chippmann/ch.hippmann.godot.utilities"

publish {
    mavenCentralUser = project.propOrEnv("MAVEN_CENTRAL_USERNAME")
    mavenCentralPassword = project.propOrEnv("MAVEN_CENTRAL_PASSWORD")
    gpgInMemoryKey = project.propOrEnv("GPG_IN_MEMORY_KEY")
    gpgPassword = project.propOrEnv("GPG_PASSWORD")

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