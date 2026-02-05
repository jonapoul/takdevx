import takdevx.gradle.pluginDependency

plugins {
  id("takdevx.plugin")
}

gradlePlugin {
  plugins {
    create("dependency-guard") {
      implementationClass = "takdevx.dependencyguard.TakDependencyGuardPlugin"
      displayName = "TAK Dependency Guard"
      tags.addAll("guard", "dependency", "baseline", "check")
    }
  }
}

dependencies {
  api(pluginDependency(libs.plugins.dependencyGuard))
  compileOnly(pluginDependency(libs.plugins.kotlin))
  testImplementation(project(":takdevx-test"))
  testPluginClasspath(pluginDependency(libs.plugins.agp.app))
  testPluginClasspath(pluginDependency(libs.plugins.kotlin))
  testPluginClasspath(pluginDependency(libs.plugins.dependencyGuard))
}
