package takdevx

import blueprint.core.localProperties
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import takdevx.internal.DevKit
import takdevx.internal.TakdevConfig
import takdevx.internal.devKit
import takdevx.internal.log

public class TakdevxSettingsPlugin : Plugin<Settings> {
  override fun apply(settings: Settings): Unit = with(settings) {
    val config = TakdevConfig(
      providers = providers,
      localProperties = localProperties(),
      rootDir = rootProject.projectDir,
    )

    if (config.mavenOnly.get()) {
      configureMaven(config)
    } else {
      when (val devKit = devKit(config.sdkPath)) {
        null -> configureMaven(config)
        else -> configureOffline(devKit, config)
      }
    }
  }

  private fun Settings.configureMaven(config: TakdevConfig) {
    log(config, "Configuring Maven TAK plugin build")

    dependencyResolutionManagement.apply {
      @Suppress("UnstableApiUsage")
      repositories.apply {
        maven { m ->
          m.setUrl(config.repoUrl.get())
          m.credentials.apply {
            username = config.repoUser.get()
            password = config.repoPassword.get()
          }
        }

        if (config.requireMavenLocal.get()) {
          mavenLocal()
        }
      }
    }
  }

  private fun Settings.configureOffline(devKit: DevKit, properties: TakdevConfig) {
    log(properties, "Configuring offline TakDev plugin build with $devKit")

    // TBC?
  }
}
