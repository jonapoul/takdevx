package takdevx

import blueprint.core.localProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import takdevx.internal.TakdevConfig
import takdevx.internal.configureMaven
import takdevx.internal.configureOffline
import takdevx.internal.devKit
import takdevx.internal.registerConfigCheckTask
import takdevx.internal.registerFlavors
import takdevx.internal.registerManifestModification
import takdevx.internal.registerTasks

public class TakdevxProjectPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    @Suppress("UnstableApiUsage")
    val config = TakdevConfig(
      providers = providers,
      localProperties = localProperties(),
      rootDir = rootProject.isolated.projectDirectory.asFile,
    )

    // Validate configuration early
    validateConfig(config)

    registerConfigCheckTask(config)
    registerManifestModification(config)
    registerTasks(config)
    registerFlavors(config)

    // Configure dependencies based on availability
    if (config.mavenOnly.get()) {
      configureMaven(config)
    } else {
      when (val devKit = devKit(config.sdkPath)) {
        null -> configureMaven(config)
        else -> configureOffline(devKit, config)
      }
    }
  }

  private fun Project.validateConfig(config: TakdevConfig) {
    // Validate ATAK_VERSION format
    val devkitVersion = config.devkitVersion.get()
    require(devkitVersion.matches(Regex("""\d+\.\d+\.\d+.*"""))) {
      """
      |Invalid ATAK_VERSION format: '$devkitVersion'
      |Expected format: X.Y.Z (e.g., 5.4.0)
      |
      |Set ATAK_VERSION in gradle.properties or via -PATAK_VERSION=...
      """.trimMargin()
    }

    // Validate Maven configuration if mavenOnly is true
    if (config.mavenOnly.get()) {
      require(config.repoUrl.isPresent) {
        """
        |mavenOnly=true requires repository URL to be configured
        |
        |Set takrepo.url in local.properties or gradle.properties
        |Example: takrepo.url=https://your-maven-repo.com/repository
        """.trimMargin()
      }

      require(config.repoUser.isPresent && config.repoPassword.isPresent) {
        """
        |mavenOnly=true requires repository credentials to be configured
        |
        |Set in local.properties:
        |  takrepo.user=your-username
        |  takrepo.password=your-password
        """.trimMargin()
      }
    }

    // Warn if using Maven but credentials aren't set
    if (!config.mavenOnly.get() && config.repoUrl.isPresent) {
      if (!config.repoUser.isPresent || !config.repoPassword.isPresent) {
        logger.warn(
          """
          |Repository URL is configured but credentials are missing
          |Maven resolution may fail if authentication is required
          |
          |Set in local.properties:
          |  takrepo.user=your-username
          |  takrepo.password=your-password
          """.trimMargin(),
        )
      }
    }
  }
}
