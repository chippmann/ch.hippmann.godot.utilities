plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.godot.kotlin.jvm) apply false
    alias(libs.plugins.grgit)
}

val versionString = libs.versions.godot.kotlin.jvm.utilities.get()
subprojects {
    group = "ch.hippmann.godot"
    version = versionString
}

val baseUrl = "github.com/chippmann/ch.hippmann.godot.utilities"
tasks {
    val generateChangelog by registering {
        group = "changelog"

        doLast {
            val tags = grgit.tag.list().reversed().filter { !it.name.endsWith("-SNAPSHOT") }
            val fromTag = tags.getOrNull(1) ?: grgit.log().last()
            val toTag = tags.getOrNull(0)
            val changeLogPrefix = """
                **Changelog:**
                
            """.trimIndent()

            val changelogString = grgit.log {
                range(fromTag, toTag?.name)
            }
                .joinToString(separator = "\n", prefix = changeLogPrefix) { commit ->
                    val link = "https://$baseUrl/commit/${commit.id}"
                    "- [${commit.abbreviatedId}]($link) ${commit.shortMessage}"
                }

            project.layout.buildDirectory.asFile.get().resolve("changelog.md").also {
                if (!it.parentFile.exists()) {
                    it.parentFile.mkdirs()
                }
            }.writeText(changelogString)
        }
    }
}