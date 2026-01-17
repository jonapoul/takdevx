package takdevx.dependencyguard

import assertk.assertThat
import assertk.assertions.containsMatch
import takdevx.dependencyguard.test.PLUGIN_ID
import takdevx.dependencyguard.test.ScenarioTest
import takdevx.dependencyguard.test.runTask
import kotlin.test.Test

class NoRestrictionsFile : ScenarioTest() {
  override val subprojectBuildFiles = mapOf(
    "app" to """
      plugins {
        kotlin("jvm")
        id("$PLUGIN_ID")
      }

      takDependencyGuard {
        configuration("runtimeClasspath")
        restrictionsFile = rootProject.file("some-other-file.txt")
      }
    """.trimIndent(),
  )

  override val otherFiles = mapOf(
    "app/dependencies/runtimeClasspath.txt" to """
      a.b.c:x.y.z:1.2.3
    """.trimIndent(),
  )

  @Test
  fun `Fail if restrictions file doesn't exist`() = runScenario {
    val result = runTask(":app:checkTakDependencies").buildAndFail()

    assertThat(result.output).containsMatch(
      "'restrictionsFile' specifies file '.*?/some-other-file.txt' which doesn't exist.".toRegex(),
    )
  }
}
