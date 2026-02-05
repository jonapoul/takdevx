package takdevx.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency
import java.io.File
import java.util.Properties

fun pluginDependency(plugin: Provider<PluginDependency>): Provider<String> =
  plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

fun Project.androidHome(): File? {
  val androidHome = System.getenv("ANDROID_HOME")?.let(::File)
  if (androidHome?.exists() == true) {
    logger.info("Using system environment variable $androidHome as ANDROID_HOME")
    return androidHome
  }

  val localProps = rootProject.file("local.properties")
  if (localProps.exists()) {
    val properties = Properties()
    localProps.inputStream().use { properties.load(it) }
    val sdkHome = properties.getProperty("sdk.dir")?.let(::File)
    if (sdkHome?.exists() == true) {
      logger.info("Using local.properties sdk.dir $sdkHome as ANDROID_HOME")
      return sdkHome
    }
  }

  logger.warn("No Android SDK found - Android unit tests will be skipped")
  return null
}

val Project.pluginId: String
  get() = properties["takdevx.pluginId"]?.toString() ?: error("Missing pluginId")
