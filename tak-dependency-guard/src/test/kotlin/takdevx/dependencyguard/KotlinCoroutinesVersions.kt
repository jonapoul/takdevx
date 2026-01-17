package takdevx.dependencyguard

import assertk.assertThat
import takdevx.dependencyguard.test.PLUGIN_ID
import takdevx.dependencyguard.test.ScenarioTest
import takdevx.dependencyguard.test.outputContains
import takdevx.dependencyguard.test.runTask
import takdevx.dependencyguard.test.taskFailed
import kotlin.test.Test

class KotlinCoroutinesVersions : ScenarioTest() {
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
      org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0
      org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.0
      org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.8.1
    """.trimIndent(),

    "restrictions.txt" to """
      org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1
      org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.1
      org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.8.1
    """.trimIndent(),
  )

  @Test
  fun `Detect kotlinx-coroutines version violations`() = runScenario {
    val result = runTask(":app:checkTakDependencies").buildAndFail()

    assertThat(result)
      .taskFailed(":app:checkTakDependencies")
      .outputContains("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0 > 1.8.1")
  }
}
