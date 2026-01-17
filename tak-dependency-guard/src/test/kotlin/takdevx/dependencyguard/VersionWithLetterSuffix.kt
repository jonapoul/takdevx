package takdevx.dependencyguard

import assertk.assertThat
import takdevx.dependencyguard.test.PLUGIN_ID
import takdevx.dependencyguard.test.ScenarioTest
import takdevx.dependencyguard.test.outputContains
import takdevx.dependencyguard.test.runTask
import takdevx.dependencyguard.test.taskFailed
import takdevx.dependencyguard.test.taskSucceeded
import kotlin.test.Test

class VersionWithLetterSuffix : ScenarioTest() {
  override val subprojectBuildFiles = mapOf(
    "app" to """
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

  override val otherFiles = mapOf(
    "restrictions.txt" to """
      gov.tak.thirdparty:libLAS:1.8.2i
      gov.tak.thirdparty:other:1.8.2i
      gov.tak.thirdparty:another:1.8.2i
    """.trimIndent(),
  )

  @Test
  fun `Fail if non-numeric versions aren't within restrictions`() = runScenario {
    resolveAndMkdirs("app/dependencies/runtimeClasspath.txt").writeText(
      """
        gov.tak.thirdparty:libLAS:1.8.2i
        gov.tak.thirdparty:other:1.8.2j
        gov.tak.thirdparty:another:1.8.3a
      """.trimIndent(),
    )

    val result = runTask(":app:checkTakDependencies").buildAndFail()

    assertThat(result)
      .taskFailed(":app:checkTakDependencies")
      .outputContains("gov.tak.thirdparty:other:1.8.2j > 1.8.2i")
      .outputContains("gov.tak.thirdparty:another:1.8.3a > 1.8.2i")
  }

  @Test
  fun `Succeed if non-numeric versions are within restrictions`() = runScenario {
    resolveAndMkdirs("app/dependencies/runtimeClasspath.txt").writeText(
      """
        gov.tak.thirdparty:libLAS:1.8.1i
        gov.tak.thirdparty:other:1.8.2e
        gov.tak.thirdparty:another:1.8.2i
      """.trimIndent(),
    )

    val result = runTask(":app:checkTakDependencies").build()

    assertThat(result)
      .taskSucceeded(":app:checkTakDependencies")
  }
}
