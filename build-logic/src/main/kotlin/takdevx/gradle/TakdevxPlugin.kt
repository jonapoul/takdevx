package takdevx.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class TakdevxPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(TakdevxBasePlugin::class)
      apply(ConventionGradle::class)
      apply(ConventionPublish::class)
    }
  }
}
