package takdevx.dependencyguard.internal

import org.gradle.api.Project
import takdevx.dependencyguard.TakDependencyGuardExtension

internal fun Project.warnIfTooManyOptionsSet(extension: TakDependencyGuardExtension) {
  val version = extension.atakVersion.orNull
  val localFile = extension.restrictionsFile.orNull
  val customUrl = extension.restrictionsUrl.orNull

  val setConfigs = mapOf(
    "atakVersion" to version,
    "restrictionsFile" to localFile?.toString(),
    "restrictionsUrl" to customUrl,
  ).mapNotNull { (k, v) -> if (v == null) null else k to v }

  if (setConfigs.size > 1) {
    val props = setConfigs.joinToString(separator = ", ") { (k, v) -> "$k = $v" }
    logger.warn("Only one configuration property allowed in takDependencyGuard! Set properties = [$props]")
  } else if (setConfigs.isEmpty() && atakVersionProperty.orNull == null) {
    logger.warn("takDependencyGuard requires configuration - none found!")
  }
}
