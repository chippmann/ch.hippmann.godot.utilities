package publish

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishPlugin
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication

abstract class PublishExtension(
    private val project: Project,
) {
    val mavenCentralUser: Property<String> = project.objects.property(String::class.java)
    val mavenCentralPassword: Property<String> = project.objects.property(String::class.java)
    val gpgInMemoryKey: Property<String> = project.objects.property(String::class.java)
    val gpgPassword: Property<String> = project.objects.property(String::class.java)

    fun pom(configure: Action<in MavenPom>) {
        project.extensions.getByType(PublishingExtension::class.java).publications.withType(MavenPublication::class.java).configureEach { publication ->
            project.afterEvaluate {
                publication.pom(configure)
            }
        }
    }
}

class PublishToMavenCentralPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(org.gradle.api.publish.maven.plugins.MavenPublishPlugin::class.java)
        val extension = target.extensions.create("publishConfig", PublishExtension::class.java)

        target.afterEvaluate { evaluatedProject ->
            val mavenCentralUser = extension.mavenCentralUser.orNull
            val mavenCentralPassword = extension.mavenCentralPassword.orNull
            val gpgInMemoryKey = extension.gpgInMemoryKey.orNull
            val gpgPassword = extension.gpgPassword.orNull

            val canSign = mavenCentralUser != null && mavenCentralPassword != null && gpgInMemoryKey != null && gpgPassword != null

            if (canSign) {
                evaluatedProject.logger.info("Will sign artifact for project \"${evaluatedProject.name}\" and setup publishing")

                evaluatedProject.pluginManager.apply(MavenPublishPlugin::class.java)
                evaluatedProject.extensions.getByType(MavenPublishBaseExtension::class.java).apply {
                    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
                    signAllPublications()
                }
            } else {
                evaluatedProject.logger.warn("Cannot sign project \"${evaluatedProject.name}\" as credentials are missing. Will not setup signing and remote publishing credentials. Publishing will only work to maven local!")
            }
        }
    }
}
