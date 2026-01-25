package takdevx.dependencyguard

import assertk.assertThat
import takdevx.test.PLUGIN_ID
import takdevx.test.outputContains
import takdevx.test.runTask
import takdevx.test.taskFailed
import kotlin.test.Test

class MultipleDependencyConfigurations : DependencyGuardScenarioTest() {
  override val subprojectBuildFiles = mapOf(
    "app" to """
      plugins {
        kotlin("jvm")
        id("$PLUGIN_ID")
      }

      takDependencyGuard {
        configuration("compileClasspath")
        configuration("runtimeClasspath")
        restrictionsFile = rootProject.file("restrictions.txt")
      }
    """.trimIndent(),
  )

  override val otherFiles = mapOf(
    "app/dependencies/compileClasspath.txt" to """
      androidx.core:core:1.18.0
    """.trimIndent(),

    "app/dependencies/runtimeClasspath.txt" to """
      androidx.fragment:fragment:1.9.0
    """.trimIndent(),

    "restrictions.txt" to """
      androidx.core:core:1.17.0
      androidx.fragment:fragment:1.8.9
    """.trimIndent(),
  )

  @Test
  fun `Check all configuration files and report violations separately`() = runScenario {
    val result = runTask(":app:checkTakDependencies").buildAndFail()

    assertThat(result)
      .taskFailed(":app:checkTakDependencies")
      .outputContains("compileClasspath")
      .outputContains("runtimeClasspath")
      .outputContains("androidx.core:core:1.18.0 > 1.17.0")
      .outputContains("androidx.fragment:fragment:1.9.0 > 1.8.9")
  }
}
