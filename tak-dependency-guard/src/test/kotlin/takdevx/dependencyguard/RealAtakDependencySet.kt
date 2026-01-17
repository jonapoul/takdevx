package takdevx.dependencyguard

import assertk.assertThat
import takdevx.dependencyguard.test.PLUGIN_ID
import takdevx.dependencyguard.test.ScenarioTest
import takdevx.dependencyguard.test.outputContains
import takdevx.dependencyguard.test.runTask
import takdevx.dependencyguard.test.taskFailed
import kotlin.test.Test

class RealAtakDependencySet : ScenarioTest() {
  override val rootBuildFile = """
    plugins {
      kotlin("jvm")
      id("$PLUGIN_ID")
    }

    takDependencyGuard {
      configuration("runtimeClasspath")
      restrictionsFile = file("restrictions.txt")
    }
  """.trimIndent()

  override val otherFiles = mapOf(
    "dependencies/runtimeClasspath.txt" to """
      androidx.core:core:1.17.0
      androidx.fragment:fragment:1.8.9
      androidx.lifecycle:lifecycle-runtime:2.9.4
      com.caverock:androidsvg-aar:1.4
      com.squareup.okhttp3:okhttp:4.11.0
      gov.tak.thirdparty:libLAS:1.8.2i
      org.jetbrains.kotlin:kotlin-stdlib:2.2.0
      org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0
    """.trimIndent(),

    "restrictions.txt" to """
      androidx.core:core:1.17.0
      androidx.fragment:fragment:1.8.9
      androidx.lifecycle:lifecycle-runtime:2.9.4
      com.caverock:androidsvg-aar:1.4
      com.squareup.okhttp3:okhttp:4.11.0
      gov.tak.thirdparty:libLAS:1.8.2i
      org.jetbrains.kotlin:kotlin-stdlib:2.2.0
      org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1
    """.trimIndent(),
  )

  @Test
  fun `Detect violation in realistic ATAK dependency set`() = runScenario {
    val result = runTask(":checkTakDependencies").buildAndFail()

    assertThat(result)
      .taskFailed(":checkTakDependencies")
      .outputContains("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0 > 1.8.1")
  }
}
