package takdevx.gradle

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.plugin.DetektPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.withType

class ConventionDetekt : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(DetektPlugin::class)
    }

    extensions.configure(DetektExtension::class) {
      @Suppress("UnstableApiUsage")
      config.from(rootProject.isolated.projectDirectory.file("config/detekt.yml"))
      buildUponDefaultConfig.set(true)
    }

    val detektTasks = tasks.withType(Detekt::class)

    val detektCheck by tasks.registering { dependsOn(detektTasks) }

    tasks.named("check") { dependsOn(detektCheck) }

    detektTasks.configureEach {
      exclude { it.path.contains("generated") }
      reports {
        html { required.set(true) }
        sarif { required.set(true) }
      }
    }
  }
}
