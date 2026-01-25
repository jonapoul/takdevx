package takdevx.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.plugin.GradlePluginApiVersion
import org.gradle.api.attributes.plugin.GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
import org.gradle.plugin.devel.tasks.ValidatePlugins

class ConventionGradle : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(JavaGradlePluginPlugin::class)
    }

    tasks.withType(ValidatePlugins::class).configureEach {
      enableStricterValidation.set(true)
      failOnWarning.set(true)
    }

    configurations.named("apiElements").configure {
      attributes {
        attribute(
          GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
          objects.named<GradlePluginApiVersion>(providers.gradleProperty("takdevx.minimumGradleVersion").get()),
        )
      }
    }

    extensions.configure(GradlePluginDevelopmentExtension::class) {
      vcsUrl.set("https://github.com/jonapoul/takdevx.git")
      website.set("https://github.com/jonapoul/takdevx")
      plugins.configureEach {
        id = project.pluginId
        description = properties["POM_DESCRIPTION"]?.toString()
        tags.addAll("gradle", "atak", "tak")
      }
    }
  }
}
