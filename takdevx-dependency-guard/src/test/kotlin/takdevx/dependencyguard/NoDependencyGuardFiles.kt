package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.failsBuild
import blueprint.test.outputContains
import blueprint.test.taskFailed
import takdevx.test.PLUGIN_ID
import kotlin.test.Test

class NoDependencyGuardFiles : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    "restrictions.txt"("androidx.core:core:1.17.0")

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
      """.trimIndent(),
    )
  }

  @Test
  fun `Fail when no dependency guard files exist`() = runScenario {
    // Create empty dependencies directory
    rootDir.resolve("app/dependencies").mkdirs()

    assertThatTask(":app:checkTakDependencies", "-x", "dependencyGuard")
      .failsBuild()
      .taskFailed(":app:checkTakDependencies")
      .outputContains("No dependency guard files found")
  }
}
