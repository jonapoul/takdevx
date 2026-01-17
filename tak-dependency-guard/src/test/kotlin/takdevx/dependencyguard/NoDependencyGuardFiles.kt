package takdevx.dependencyguard

import assertk.assertThat
import takdevx.dependencyguard.test.PLUGIN_ID
import takdevx.dependencyguard.test.ScenarioTest
import takdevx.dependencyguard.test.outputContains
import takdevx.dependencyguard.test.runTask
import takdevx.dependencyguard.test.taskFailed
import kotlin.test.Test

class NoDependencyGuardFiles : ScenarioTest() {
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
      androidx.core:core:1.17.0
    """.trimIndent(),
  )

  @Test
  fun `Fail when no dependency guard files exist`() = runScenario {
    // Create empty dependencies directory
    resolve("app/dependencies").mkdirs()

    val result = runTask(":app:checkTakDependencies").buildAndFail()

    assertThat(result)
      .taskFailed(":app:checkTakDependencies")
      .outputContains("No dependency guard files found")
  }
}
