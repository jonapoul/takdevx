package takdevx

import blueprint.core.localProperties
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

public class TakdevxSettingsPlugin : Plugin<Settings> {
  override fun apply(settings: Settings): Unit = with(settings) {
    gradle.beforeProject { project ->
      project.repositories.maven { repo ->
        repo.url = project.uri(
          project.rootProject.layout.projectDirectory
            .dir(".takdev/aars"),
        )
      }
    }
  }

  private fun Settings.initialise(extension: TakdevxSettingsExtension) = with(extension) {
    val localProperties = localProperties()

    fun string(key: String) = localProperties.map { it[key] }.orElse(providers.gradleProperty(key))
    fun bool(key: String) = string(key).map(String::toBoolean)

    mavenOnly.convention(bool("takdevx.mavenOnly").orElse(false))
  }
}
