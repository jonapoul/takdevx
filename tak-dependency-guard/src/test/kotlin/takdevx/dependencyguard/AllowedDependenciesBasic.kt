package takdevx.dependencyguard

import assertk.assertThat
import takdevx.dependencyguard.test.PLUGIN_ID
import takdevx.dependencyguard.test.ScenarioTest
import takdevx.dependencyguard.test.outputContains
import takdevx.dependencyguard.test.outputDoesNotContain
import takdevx.dependencyguard.test.runTask
import takdevx.dependencyguard.test.taskFailed
import takdevx.dependencyguard.test.taskSucceeded
import kotlin.test.Test

class AllowedDependenciesBasic : ScenarioTest() {
  override val subprojectBuildFiles = mapOf(
    "app" to """
      plugins {
        kotlin("jvm")
        id("$PLUGIN_ID")
      }

      takDependencyGuard {
        configuration("runtimeClasspath")
        restrictionsFile = rootProject.file("restrictions.txt")

        // Allow androidx.core:core:1.18.0 to bypass restriction
        allow("androidx.core", "core", "1.18.0")
      }
    """.trimIndent(),
  )

  override val otherFiles = mapOf(
    "app/dependencies/runtimeClasspath.txt" to """
      androidx.core:core:1.18.0
      androidx.fragment:fragment:1.9.0
    """.trimIndent(),

    "restrictions.txt" to """
      androidx.core:core:1.17.0
      androidx.fragment:fragment:1.8.9
    """.trimIndent(),
  )

  @Test
  fun `Allowed dependency bypasses restriction while non-allowed dependency still fails`() = runScenario {
    val result = runTask(":app:checkTakDependencies").buildAndFail()

    assertThat(result)
      .taskFailed(":app:checkTakDependencies")
      .outputContains("androidx.fragment:fragment:1.9.0 > 1.8.9")
      .outputDoesNotContain("androidx.core:core:1.18.0")
  }

  @Test
  fun `All dependencies pass when all violators are in allowlist`() = runScenario {
    // Create a scenario where the only violation is allowed
    val customBuildFile = """
      plugins {
        kotlin("jvm")
        id("$PLUGIN_ID")
      }

      takDependencyGuard {
        configuration("runtimeClasspath")
        restrictionsFile = rootProject.file("restrictions.txt")

        // Allow both dependencies that would otherwise fail
        allow("androidx.core", "core", "1.18.0")
        allow("androidx.fragment", "fragment", "1.9.0")
      }
    """.trimIndent()

    resolve("app/build.gradle.kts").writeText(customBuildFile)

    val result = runTask(":app:checkTakDependencies").build()

    assertThat(result)
      .taskSucceeded(":app:checkTakDependencies")
  }
}
