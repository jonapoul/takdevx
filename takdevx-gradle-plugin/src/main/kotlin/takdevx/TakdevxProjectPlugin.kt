package takdevx

import blueprint.core.localProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import takdevx.internal.registerFlavors
import takdevx.internal.registerManifestModification
import takdevx.internal.registerTakDevLintTask
import takdevx.internal.registerTasks

public class TakdevxProjectPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    val extension = extensions.create("takdevX", TakdevxProjectExtension::class.java)
    setDefaults(extension)

    registerTakDevLintTask(extension)
    registerManifestModification(extension)

    val isDevKitEnabled = providers
      .gradleProperty("isDevKitEnabled")
      .map(String::toBoolean)
      .orElse(provider { "takrepo.url" in properties })

    // TODO: if (project.mavenOnly) {

    registerTasks(extension)
    // TODO addUtilities

    registerFlavors()
  }

  private fun Project.setDefaults(extension: TakdevxProjectExtension) = with(extension) {
    @Suppress("UnstableApiUsage")
    val rootDir = rootProject.isolated.projectDirectory
    val thisDir = layout.projectDirectory
    val localProperties = localProperties()

    fun string(key: String) = localProperties.map { it[key] }.orElse(providers.gradleProperty(key))
    fun bool(key: String) = string(key).map(String::toBoolean)
    fun int(key: String) = string(key).map(String::toInt)
    fun dir(key: String) = string(key).map(thisDir::dir)

    val atakVersion = providers.gradleProperty("ATAK_VERSION")
    devkitVersion.convention(string("takdevx.devkitVersion").orElse(atakVersion))
    verbose.convention(bool("takdevx.verbose").orElse(false))
    pluginId.convention(string("takdevx.metadata.pluginId"))
    snapshot.convention(bool("takdevx.snapshot").orElse(false))
    requireMavenLocal.convention(bool("takdevx.requireMavenLocal").orElse(false))
    sdkPath.convention(dir("takdevx.sdkPath").orElse(rootDir.dir("sdk")))
    production.convention(bool("takdevx.production").orElse(false))
    noApp.convention(bool("takdevx.noApp").orElse(false))
    conTestEnable.convention(bool("takdevx.conTestEnable").orElse(false))
    staticVersion.convention(int("takdevx.staticVersion").orElse(-1))
    conTestVersion.convention(string("takdevx.conTestVersion").orElse(devkitVersion))
    conTestPath.convention(dir("takdevx.conTestPath").orElse(rootDir.dir("espresso")))
    metadataPluginId.convention(string("takdevx.metadataPluginId"))
  }
}
