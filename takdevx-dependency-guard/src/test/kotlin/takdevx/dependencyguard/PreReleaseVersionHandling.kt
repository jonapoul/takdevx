package takdevx.dependencyguard

import assertk.assertThat
import takdevx.test.PLUGIN_ID
import takdevx.test.runTask
import takdevx.test.taskSucceeded
import kotlin.test.Test

class PreReleaseVersionHandling : DependencyGuardScenarioTest() {
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
      androidx.core:core:1.17.0-alpha01
      androidx.fragment:fragment:1.8.0-beta02
    """.trimIndent(),

    "restrictions.txt" to """
      androidx.core:core:1.17.0
      androidx.fragment:fragment:1.8.9
    """.trimIndent(),
  )

  @Test
  fun `Pre-release versions should be less than stable versions`() = runScenario {
    val result = runTask(":app:checkTakDependencies").build()

    assertThat(result)
      .taskSucceeded(":app:checkTakDependencies")
  }
}
