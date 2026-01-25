package takdevx.dependencyguard

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import takdevx.dependencyguard.internal.registerCheckTakDependenciesTask
import takdevx.dependencyguard.internal.registerDownloadFileTask
import takdevx.dependencyguard.internal.warnIfTooManyOptionsSet

/**
 * Gradle plugin that validates dependencies in ATAK (Android Tactical Assault Kit) plugins.
 *
 * This plugin ensures that ATAK plugin dependencies don't exceed version restrictions specified by the ATAK SDK. It
 * wraps Dropbox's `dependency-guard` plugin and adds TAK-specific validation by comparing resolved dependencies
 * against known ATAK version restrictions.
 *
 * The plugin:
 * - Creates a `takDependencyGuard` extension for configuration
 * - Registers a `downloadTakDependencies` task to download restriction files
 * - Registers a `checkTakDependencies` task to validate dependencies
 * - Hooks into the `check` task to run validation automatically
 *
 * Usage:
 * ```kotlin
 * plugins {
 *   id("dev.jonpoulton.takdevx.dependency-guard")
 * }
 *
 * takDependencyGuard {
 *   // Configure which configurations to track
 *   configuration("runtimeClasspath")
 *
 *   // Configure TAK version restrictions (choose one):
 *   atakVersion = "5.6.0"                              // Download from GitHub
 *   restrictionsUrl = "https://example.com/file.txt"   // Download from custom URL
 *   restrictionsFile = file("restrictions.txt")        // Use local file
 * }
 * ```
 *
 * @see TakDependencyGuardExtension for configuration options
 * @see CheckDependencies for validation details
 */
public class TakDependencyGuardPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    pluginManager.apply(DependencyGuardPlugin::class.java)

    val extension = extensions.create("takDependencyGuard", TakDependencyGuardExtension::class.java)
    val downloadFile = registerDownloadFileTask(extension)
    val checkDependencies = registerCheckTakDependenciesTask(extension, downloadFile)

    pluginManager.withPlugin("base") {
      tasks.named(CHECK_TASK_NAME).configure { t ->
        t.dependsOn(checkDependencies)
      }
    }

    afterEvaluate {
      warnIfTooManyOptionsSet(extension)
    }
  }
}
