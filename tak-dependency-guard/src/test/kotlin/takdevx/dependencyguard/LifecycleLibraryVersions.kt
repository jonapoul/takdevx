package takdevx.dependencyguard

import assertk.assertThat
import takdevx.dependencyguard.test.PLUGIN_ID
import takdevx.dependencyguard.test.ScenarioTest
import takdevx.dependencyguard.test.runTask
import takdevx.dependencyguard.test.taskSucceeded
import kotlin.test.Test

class LifecycleLibraryVersions : ScenarioTest() {
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
      androidx.lifecycle:lifecycle-common-jvm:2.9.4
      androidx.lifecycle:lifecycle-common:2.9.4
      androidx.lifecycle:lifecycle-livedata-core-ktx:2.9.4
      androidx.lifecycle:lifecycle-livedata-core:2.9.4
      androidx.lifecycle:lifecycle-runtime-android:2.9.4
      androidx.lifecycle:lifecycle-viewmodel-android:2.9.4
    """.trimIndent(),

    "restrictions.txt" to """
      androidx.lifecycle:lifecycle-common-jvm:2.9.4
      androidx.lifecycle:lifecycle-common:2.9.4
      androidx.lifecycle:lifecycle-livedata-core-ktx:2.9.4
      androidx.lifecycle:lifecycle-livedata-core:2.9.4
      androidx.lifecycle:lifecycle-runtime-android:2.9.4
      androidx.lifecycle:lifecycle-viewmodel-android:2.9.4
    """.trimIndent(),
  )

  @Test
  fun `Validate multiple lifecycle library versions matching ATAK restrictions`() = runScenario {
    val result = runTask(":app:checkTakDependencies").build()

    assertThat(result)
      .taskSucceeded(":app:checkTakDependencies")
  }
}
