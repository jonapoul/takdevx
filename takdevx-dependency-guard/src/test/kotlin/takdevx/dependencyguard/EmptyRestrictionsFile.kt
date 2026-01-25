package takdevx.dependencyguard

import assertk.assertThat
import takdevx.test.PLUGIN_ID
import takdevx.test.outputContains
import takdevx.test.runTask
import takdevx.test.taskFailed
import kotlin.test.Test

class EmptyRestrictionsFile : DependencyGuardScenarioTest() {
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
    "app/dependencies/runtimeClasspath.txt" to """
      androidx.core:core:1.17.0
    """.trimIndent(),

    "restrictions.txt" to "",
  )

  @Test
  fun `Fail when restrictions file is empty`() = runScenario {
    val result = runTask(":app:checkTakDependencies").buildAndFail()

    assertThat(result)
      .taskFailed(":app:checkTakDependencies")
      .outputContains("No versions found in .*?/restrictions.txt".toRegex())
  }
}
