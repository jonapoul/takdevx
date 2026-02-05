package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.failsBuild
import blueprint.test.outputContains
import blueprint.test.taskFailed
import takdevx.test.PLUGIN_ID
import kotlin.test.Test

class KotlinCoroutinesVersions : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    "restrictions.txt"(
      """
        org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1
        org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.8.1
        org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.8.1
      """.trimIndent(),
    )

    appBuildGradleKts(
      """
        plugins {
          kotlin("jvm")
          id("$PLUGIN_ID")
        }

        takDependencyGuard {
          configuration("runtimeClasspath")
          restrictionsFile = rootProject.file("restrictions.txt")
        }

        dependencies {
          implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
          implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        }
      """.trimIndent(),
    )
  }

  @Test
  fun `Detect kotlinx-coroutines version violations`() = runScenario {
    dependencyGuardBaseline()
    assertThatTask(":app:checkTakDependencies")
      .failsBuild()
      .taskFailed(":app:checkTakDependencies")
      .outputContains("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0 > 1.8.1")
  }
}
