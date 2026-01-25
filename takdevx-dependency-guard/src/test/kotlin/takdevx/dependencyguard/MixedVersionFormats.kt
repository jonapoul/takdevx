package takdevx.dependencyguard

import assertk.assertThat
import takdevx.test.PLUGIN_ID
import takdevx.test.runTask
import takdevx.test.taskSucceeded
import kotlin.test.Test

class MixedVersionFormats : DependencyGuardScenarioTest() {
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
      com.caverock:androidsvg-aar:1.4
      androidx.core:core:1.17.0
      gov.tak.thirdparty:libLAS:1.8.2i
      org.jetbrains.kotlin:kotlin-stdlib:2.2.0
      com.google.guava:listenablefuture:1.0
    """.trimIndent(),

    "restrictions.txt" to """
      com.caverock:androidsvg-aar:1.4
      androidx.core:core:1.17.0
      gov.tak.thirdparty:libLAS:1.8.2i
      org.jetbrains.kotlin:kotlin-stdlib:2.2.0
      com.google.guava:listenablefuture:1.0
    """.trimIndent(),
  )

  @Test
  fun `Handle mixed version formats from real ATAK restrictions`() = runScenario {
    val result = runTask(":app:checkTakDependencies").build()

    assertThat(result)
      .taskSucceeded(":app:checkTakDependencies")
  }
}
