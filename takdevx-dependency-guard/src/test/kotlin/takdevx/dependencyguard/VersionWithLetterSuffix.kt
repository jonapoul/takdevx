package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.failsBuild
import blueprint.test.outputContains
import blueprint.test.taskFailed
import blueprint.test.taskSucceeded
import takdevx.test.PLUGIN_ID
import kotlin.test.Test

class VersionWithLetterSuffix : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    "restrictions.txt"(
      """
        gov.tak.thirdparty:libLAS:1.8.2i
        gov.tak.thirdparty:other:1.8.2i
        gov.tak.thirdparty:another:1.8.2i
      """.trimIndent(),
    )

    appBuildGradleKts(
      """
        plugins {
          kotlin("jvm")
          id("$PLUGIN_ID")
        }

        takDependencyGuard {
          configuration("runtimeClasspath")
          restrictionsFile = rootProject.file("restrictions.txt")
        }
      """.trimIndent(),
    )
  }

  @Test
  fun `Fail if non-numeric versions aren't within restrictions`() = runScenario {
    rootDir.resolve("app/dependencies/runtimeClasspath.txt")
      .also { it.parentFile.mkdirs() }
      .writeText(
        """
          gov.tak.thirdparty:libLAS:1.8.2i
          gov.tak.thirdparty:other:1.8.2j
          gov.tak.thirdparty:another:1.8.3a
        """.trimIndent(),
      )

    assertThatTask(":app:checkTakDependencies", "-x", "dependencyGuard")
      .failsBuild()
      .taskFailed(":app:checkTakDependencies")
      .outputContains("gov.tak.thirdparty:other:1.8.2j > 1.8.2i")
      .outputContains("gov.tak.thirdparty:another:1.8.3a > 1.8.2i")
  }

  @Test
  fun `Succeed if non-numeric versions are within restrictions`() = runScenario {
    rootDir.resolve("app/dependencies/runtimeClasspath.txt")
      .also { it.parentFile.mkdirs() }
      .writeText(
        """
          gov.tak.thirdparty:libLAS:1.8.1i
          gov.tak.thirdparty:other:1.8.2e
          gov.tak.thirdparty:another:1.8.2i
        """.trimIndent(),
      )

    assertThatTask(":app:checkTakDependencies", "-x", "dependencyGuard")
      .buildsSuccessfully()
      .taskSucceeded(":app:checkTakDependencies")
  }
}
