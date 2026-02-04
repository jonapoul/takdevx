package takdevx.internal

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.io.File

internal class TakdevConfig(
  private val providers: ProviderFactory,
  private val localProperties: Provider<Map<String, String>>,
  private val rootDir: File,
) {
  val mavenOnly: Provider<Boolean> =
    bool(localKey = "takrepo.force", projectKey = "mavenOnly")

  val repoUrl: Provider<String> =
    string(localKey = "takrepo.url")

  val repoUser: Provider<String> =
    string(localKey = "takrepo.user")

  val repoPassword: Provider<String> =
    string(localKey = "takrepo.password")

  val devkitVersion: Provider<String> =
    string(localKey = "takrepo.devkit.version", projectKey = "devkitVersion")
      .orElse(providers.gradleProperty("ATAK_VERSION"))

  val verbose: Provider<Boolean> =
    bool(localKey = "takdev.verbose", projectKey = "takdev.verbose")
      .orElse(false)

  val snapshot: Provider<Boolean> =
    bool(localKey = "takrepo.snapshot", projectKey = "snapshot")
      .orElse(true)

  val requireMavenLocal: Provider<Boolean> =
    bool(localKey = "takrepo.requireMavenLocal", projectKey = "requireMavenLocal")
      .orElse(false)

  val production: Provider<Boolean> =
    bool(localKey = "takdev.production", projectKey = "takdevProduction")
      .orElse(false)

  val noApp: Provider<Boolean> =
    bool(localKey = "takdev.noapp", projectKey = "takdevNoApp")

  val conTestEnable: Provider<Boolean> =
    bool(localKey = "takdev.contest.enable", projectKey = "takdevConTestEnable")

  val staticVersion: Provider<Int> =
    int(localKey = "takStaticVersion", projectKey = "takStaticVersion")
      .orElse(-1)

  val conTestVersion: Provider<String> =
    string("takdev.contest.version", projectKey = "takdevConTestVersion")
      .orElse(devkitVersion)

  val conTestPath: Provider<File> =
    dir(localKey = "takdev.contest.path", projectKey = "takdevConTestPath")
      .orElse(rootDir.resolve("espresso"))

  val pluginId: Provider<String> =
    string(localKey = "takdev.metadata.pluginid", projectKey = "takdevMetadataPluginId")

  val sdkPath: Provider<File> =
    dir(localKey = "sdk.path", projectKey = "sdkPath")
      .orElse(rootDir.resolve("sdk"))

  private fun string(localKey: String, projectKey: String = localKey) = localProperties
    .map<String> { map -> map.get(projectKey) }
    .orElse(providers.gradleProperty(localKey))

  private fun bool(localKey: String, projectKey: String) = string(localKey, projectKey).map(String::toBoolean)
  private fun int(localKey: String, projectKey: String) = string(localKey, projectKey).map(String::toInt)
  private fun dir(localKey: String, projectKey: String) = string(localKey, projectKey).map(rootDir::resolve)
}
