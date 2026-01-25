plugins {
  `kotlin-dsl`
}

tasks.validatePlugins {
  enableStricterValidation = true
  failOnWarning = true
}

dependencies {
  fun compileOnlyPlugin(plugin: Provider<PluginDependency>) =
    compileOnly(plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}" })

  compileOnlyPlugin(libs.plugins.buildConfig)
  compileOnlyPlugin(libs.plugins.dependencyAnalysis)
  compileOnlyPlugin(libs.plugins.dependencyGuard)
  compileOnlyPlugin(libs.plugins.detekt)
  compileOnlyPlugin(libs.plugins.dokka)
  compileOnlyPlugin(libs.plugins.kotlin)
  compileOnlyPlugin(libs.plugins.kotlinAbi)
  compileOnlyPlugin(libs.plugins.licensee)
  compileOnlyPlugin(libs.plugins.publish)
  compileOnlyPlugin(libs.plugins.publishReport)

  implementation(libs.blueprint)
}

gradlePlugin {
  plugins {
    operator fun String.invoke(impl: String) = register(this) {
      id = this@invoke
      implementationClass = impl
    }

    "takdevx.base"(impl = "takdevx.gradle.TakdevxBasePlugin")
    "takdevx.plugin"(impl = "takdevx.gradle.TakdevxPlugin")
    "takdevx.convention.gradle"(impl = "takdevx.gradle.ConventionGradle")
  }
}
