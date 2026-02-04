package takdevx.internal

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Provider
import takdevx.TakdevxSettingsPlugin

internal fun Project.log(
  properties: TakdevConfig,
  message: String,
) = logger.logMethod(properties.verbose)(message)

@Suppress("UnusedReceiverParameter")
internal fun Settings.log(
  properties: TakdevConfig,
  message: String,
) = SETTINGS_LOGGER.logMethod(properties.verbose)(message)

internal fun Task.log(verbose: Provider<Boolean>, message: String) = logger.logMethod(verbose)(message)

private fun Logger.logMethod(verbose: Provider<Boolean>): (String) -> Unit = when (verbose.get()) {
  true -> ::lifecycle
  false -> ::info
}

private val SETTINGS_LOGGER: Logger = Logging.getLogger(TakdevxSettingsPlugin::class.java)
