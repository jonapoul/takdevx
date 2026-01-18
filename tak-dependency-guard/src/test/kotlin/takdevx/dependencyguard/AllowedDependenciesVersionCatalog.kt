package takdevx.dependencyguard

import assertk.assertThat
import takdevx.dependencyguard.test.PLUGIN_ID
import takdevx.dependencyguard.test.ScenarioTest
import takdevx.dependencyguard.test.outputDoesNotContain
import takdevx.dependencyguard.test.runTask
import takdevx.dependencyguard.test.taskSucceeded
import kotlin.test.Test

class AllowedDependenciesVersionCatalog : ScenarioTest() {
  override val subprojectBuildFiles = mapOf(
    "app" to """
      plugins {
        kotlin("jvm")
        id("$PLUGIN_ID")
      }

      takDependencyGuard {
        configuration("runtimeClasspath")
        restrictionsFile = rootProject.file("restrictions.txt")

        // Use version catalog reference
        allow(libs.androidx.core)
      }
    """.trimIndent(),
  )

  override val otherFiles = mapOf(
    "gradle/libs.versions.toml" to """
      [libraries]
      androidx-core = { group = "androidx.core", name = "core", version = "1.18.0" }
    """.trimIndent(),

    "app/dependencies/runtimeClasspath.txt" to """
      androidx.core:core:1.18.0
    """.trimIndent(),

    "restrictions.txt" to """
      androidx.core:core:1.17.0
    """.trimIndent(),
  )

  @Test
  fun `Version catalog reference allows dependency to bypass restriction`() = runScenario {
    val result = runTask(":app:checkTakDependencies").build()

    assertThat(result)
      .taskSucceeded(":app:checkTakDependencies")
      .outputDoesNotContain("androidx.core:core:1.18.0")
  }
}
