package takdevx.dependencyguard

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardConfiguration
import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPluginExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

/**
 * Configuration extension for the TAK Dependency Guard plugin.
 *
 * This extension wraps Dropbox's [DependencyGuardPluginExtension] and adds TAK-specific configuration options.
 *
 * You can use all the standard `dependencyGuard` methods (like [configuration]) alongside TAK-specific properties
 * in a single configuration block:
 *
 * ```kotlin
 * takDependencyGuard {
 *   // Standard dependency-guard configuration
 *   configuration("runtimeClasspath")
 *
 *   // TAK-specific configuration
 *   atakVersion = "5.6.0"
 * }
 * ```
 *
 * Four ways to configure TAK restrictions, in descending order of priority:
 *
 * 1. Set [atakVersion] to a string like "5.6.0". This way, the plugin will attempt to download a known file for that
 * version from this project's repository on GitHub.
 * 2. Set [restrictionsUrl] to a custom URL to download the restrictions file from.
 * 3. Set [restrictionsFile] to a file on the local machine containing your dependency restrictions.
 * 4. Set the `ATAK_VERSION` Gradle property. If you're building an ATAK plugin, chances are you already have this as
 * part of your build script.
 *
 * Note that only ONE of the three properties in this extension can be set. Otherwise, the build will print a warning
 * during IDE sync, and will fail when running the `takDependencyGuard` task.
 */
public abstract class TakDependencyGuardExtension @Inject constructor(project: Project) {
  internal abstract val allowedDependencies: SetProperty<String>

  private val dependencyGuardExtension by lazy {
    project.extensions.getByType(DependencyGuardPluginExtension::class.java)
  }

  /**
   * ATAK version string (e.g. "5.6.0") to download known restrictions from GitHub.
   *
   * When set, the plugin downloads restrictions from:
   * `https://raw.githubusercontent.com/jonapoul/takdevx/main/atak-versions/restrictions-{version}.txt`
   *
   * This is the highest priority configuration option.
   *
   * Mutually exclusive with [restrictionsUrl] and [restrictionsFile].
   *
   * Example:
   * ```kotlin
   * takDependencyGuard {
   *   atakVersion = "5.6.0"
   * }
   * ```
   */
  public abstract val atakVersion: Property<String>

  /**
   * Custom URL to download restrictions file from.
   *
   * Useful when restrictions are hosted on a private or alternative server.
   * Expected file format: Each line contains `group:artifact:maxVersion`
   *
   * This has second priority (after [atakVersion]).
   *
   * Mutually exclusive with [atakVersion] and [restrictionsFile].
   *
   * Example:
   * ```kotlin
   * takDependencyGuard {
   *   restrictionsUrl = "https://internal.company.com/atak-restrictions.txt"
   * }
   * ```
   */
  public abstract val restrictionsUrl: Property<String>

  /**
   * Local file path containing dependency restrictions.
   *
   * Use this when you have a local restrictions file and don't want to download it.
   * File format: Each line contains `group:artifact:maxVersion`
   *
   * When set, the [DownloadFile] task is skipped.
   *
   * This has third priority (after [atakVersion] and [restrictionsUrl]).
   *
   * Example:
   * ```kotlin
   * takDependencyGuard {
   *   restrictionsFile = file("path/to/restrictions.txt")
   * }
   * ```
   */
  public abstract val restrictionsFile: RegularFileProperty

  /**
   * Configures a dependency configuration to be tracked by dependency-guard.
   *
   * This is a convenience method that delegates to the underlying [DependencyGuardPluginExtension].
   *
   * @param name The name of the Gradle configuration to track (e.g., "runtimeClasspath")
   */
  public fun configuration(name: String): Unit = dependencyGuardExtension.configuration(name)

  /**
   * Configures a dependency configuration to be tracked by dependency-guard with additional configuration.
   *
   * This is a convenience method that delegates to the underlying [DependencyGuardPluginExtension].
   *
   * @param name The name of the Gradle configuration to track (e.g., "runtimeClasspath")
   * @param action Additional configuration for this dependency configuration
   */
  public fun configuration(name: String, action: Action<DependencyGuardConfiguration>): Unit =
    dependencyGuardExtension.configuration(name, action)

  /**
   * Adds a dependency from a version catalog to the allowlist.
   *
   * Example:
   * ```kotlin
   * takDependencyGuard {
   *   allow(libs.squareup.okio)
   * }
   * ```
   *
   * @param dependency Version catalog dependency reference
   */
  public fun allow(dependency: Provider<MinimalExternalModuleDependency>) {
    allowedDependencies.add(
      dependency.map { dep ->
        "${dep.module.group}:${dep.module.name}:${dep.versionConstraint}"
      },
    )
  }

  /**
   * Adds a dependency to the allowlist to bypass TAK version restrictions.
   *
   * Example:
   * ```kotlin
   * takDependencyGuard {
   *   allow("androidx.core", "core", "1.17.0")
   * }
   * ```
   *
   * @param group Dependency group ID (e.g., "androidx.core")
   * @param artifact Dependency artifact ID (e.g., "core")
   * @param version Exact version string (e.g., "1.17.0")
   */
  public fun allow(group: String, artifact: String, version: String) {
    allowedDependencies.add("$group:$artifact:$version")
  }

  /**
   * Adds a dependency to the allowlist using full coordinate string.
   *
   * Example:
   * ```kotlin
   * takDependencyGuard {
   *   allow("androidx.lifecycle:lifecycle-runtime:2.9.5")
   * }
   * ```
   *
   * @param coordinate Full dependency coordinate in format "group:artifact:version"
   */
  public fun allow(coordinate: String) {
    require(coordinate.count { it == ':' } == 2) {
      "Dependency coordinate must be in format 'group:artifact:version', got: $coordinate"
    }
    allowedDependencies.add(coordinate)
  }
}
