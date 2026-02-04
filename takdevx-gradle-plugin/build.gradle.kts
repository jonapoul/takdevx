import blueprint.core.gitVersionHash
import takdevx.gradle.pluginDependency

plugins {
  id("takdevx.plugin")
}

gradlePlugin {
  plugins {
    create("atak-gradle-takdevx") {
      id = "dev.jonpoulton.takdevx"
      implementationClass = "takdevx.TakdevProjectPlugin"
      displayName = "ATAK Gradle Takdevx"
      tags.addAll("takdev", "takdevx")
    }
    create("atak-gradle-settings") {
      id = "dev.jonpoulton.takdevx.settings"
      implementationClass = "takdevx.TakdevxSettingsPlugin"
      displayName = "ATAK Gradle Settings"
      tags.addAll("takdev", "takdevx")
    }
  }
}

dependencies {
  compileOnly(pluginDependency(libs.plugins.agp.app))
  compileOnly(pluginDependency(libs.plugins.kotlin))
  implementation(libs.blueprint.core)

  testPluginClasspath(pluginDependency(libs.plugins.agp.app))
  testPluginClasspath(pluginDependency(libs.plugins.kotlin))
  testPluginClasspath(pluginDependency(libs.plugins.dependencyGuard))
}

val gitVersion = providers.gitVersionHash()
val projectVersion = providers.gradleProperty("VERSION_NAME")

tasks.withType<Jar>().configureEach {
  inputs.property("gitVersion", gitVersion)
  inputs.property("projectVersion", projectVersion)
  manifest {
    attributes(
      "Git-Version" to gitVersion.get(),
      "takdevVersion" to projectVersion.get(),
    )
  }
}
