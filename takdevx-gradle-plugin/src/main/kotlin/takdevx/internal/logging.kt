package takdevx.internal

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import takdevx.TakdevxProjectExtension

internal fun Project.log(
  extension: TakdevxProjectExtension,
  message: String,
) = logger.logMethod(extension.verbose)(message)

internal fun Task.log(verbose: Provider<Boolean>, message: String) = logger.logMethod(verbose)(message)

private fun Logger.logMethod(verbose: Provider<Boolean>): (String) -> Unit = when (verbose.get()) {
  true -> ::lifecycle
  false -> ::info
}
