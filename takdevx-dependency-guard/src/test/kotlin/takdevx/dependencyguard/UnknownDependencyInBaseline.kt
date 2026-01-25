package takdevx.dependencyguard

import assertk.assertThat
import takdevx.test.PLUGIN_ID
import takdevx.test.runTask
import takdevx.test.taskSucceeded
import kotlin.test.Test

class UnknownDependencyInBaseline : DependencyGuardScenarioTest() {
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
      com.custom:library:2.0.0
      org.unknown:dependency:5.0.0
    """.trimIndent(),

    "restrictions.txt" to """
      androidx.core:core:1.17.0
    """.trimIndent(),
  )

  @Test
  fun `Skip dependencies not present in restrictions file`() = runScenario {
    val result = runTask(":app:checkTakDependencies").build()

    assertThat(result)
      .taskSucceeded(":app:checkTakDependencies")
  }
}
