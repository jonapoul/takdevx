package takdevx.dependencyguard.test

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assumptions.assumeFalse
import takdevx.dependencyguard.test.ANDROID_HOME
import takdevx.dependencyguard.test.GRADLE_VERSION
import java.io.File

internal fun File.buildRunner(requiresAndroid: Boolean = false): GradleRunner = GradleRunner
  .create()
  .withPluginClasspath()
  .withDebug(false)
  .withGradleVersion(GRADLE_VERSION)
  .withProjectDir(this)
  .apply {
    if (requiresAndroid) {
      val home = ANDROID_HOME
      if (home == null) {
        assumeFalse(true, "No ANDROID_HOME supplied for an android test")
      } else {
        withEnvironment(mapOf("ANDROID_HOME" to home.absolutePath))
      }
    }
  }

internal fun File.runTask(
  task: String,
  requiresAndroid: Boolean = false,
  extras: List<String> = emptyList(),
): GradleRunner = buildRunner(requiresAndroid).runTask(task, extras)

internal fun GradleRunner.runTask(
  task: String,
  extras: List<String> = emptyList(),
): GradleRunner = withArguments(
  listOf(
    task,
    "--configuration-cache",
    "-Pandroid.useAndroidX=true", // needed for android builds to work, unused otherwise
  ) + extras,
)
