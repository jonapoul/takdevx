package takdevx.dependencyguard

import assertk.assertThat
import takdevx.test.PLUGIN_ID
import takdevx.test.outputContains
import takdevx.test.runTask
import takdevx.test.taskFailed
import kotlin.test.Test

class MixedVersionViolations : DependencyGuardScenarioTest() {
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
      androidx.core:core:1.15.0
      androidx.fragment:fragment:1.9.0
      androidx.appcompat:appcompat:1.5.0
    """.trimIndent(),

    "restrictions.txt" to """
      androidx.core:core:1.17.0
      androidx.fragment:fragment:1.8.9
      androidx.appcompat:appcompat:1.7.0
    """.trimIndent(),
  )

  @Test
  fun `Fail when some dependencies exceed restrictions and some don't`() = runScenario {
    val result = runTask(":app:checkTakDependencies").buildAndFail()

    assertThat(result)
      .taskFailed(":app:checkTakDependencies")
      .outputContains("androidx.fragment:fragment:1.9.0 > 1.8.9")
  }
}
