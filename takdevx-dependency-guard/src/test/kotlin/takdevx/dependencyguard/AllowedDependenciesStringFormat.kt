package takdevx.dependencyguard

import assertk.assertThat
import takdevx.test.PLUGIN_ID
import takdevx.test.outputDoesNotContain
import takdevx.test.runTask
import takdevx.test.taskSucceeded
import kotlin.test.Test

class AllowedDependenciesStringFormat : DependencyGuardScenarioTest() {
  override val subprojectBuildFiles = mapOf(
    "app" to """
      plugins {
        kotlin("jvm")
        id("$PLUGIN_ID")
      }

      takDependencyGuard {
        configuration("runtimeClasspath")
        restrictionsFile = rootProject.file("restrictions.txt")

        // Test string format: allow("group:artifact:version")
        allow("androidx.lifecycle:lifecycle-runtime:2.9.5")
      }
    """.trimIndent(),
  )

  override val otherFiles = mapOf(
    "app/dependencies/runtimeClasspath.txt" to """
      androidx.lifecycle:lifecycle-runtime:2.9.5
    """.trimIndent(),

    "restrictions.txt" to """
      androidx.lifecycle:lifecycle-runtime:2.8.0
    """.trimIndent(),
  )

  @Test
  fun `String coordinate format allows dependency to bypass restriction`() = runScenario {
    val result = runTask(":app:checkTakDependencies").build()

    assertThat(result)
      .taskSucceeded(":app:checkTakDependencies")
      .outputDoesNotContain("androidx.lifecycle:lifecycle-runtime:2.9.5")
  }
}
