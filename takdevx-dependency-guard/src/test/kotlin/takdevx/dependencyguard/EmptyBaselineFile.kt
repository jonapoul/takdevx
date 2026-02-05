package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.failsBuild
import blueprint.test.outputContainsMatch
import blueprint.test.taskFailed
import takdevx.test.PLUGIN_ID
import kotlin.test.Test

class EmptyBaselineFile : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    "restrictions.txt"("a.b.c:x.y.z:1.2.3")

    "app" {
      ("dependencies" / "runtimeClasspath.txt")("")

      "build.gradle.kts"(
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
  }

  @Test
  fun `Fail if baseline file is empty`() = runScenario {
    assertThatTask(":app:checkTakDependencies", "-x", "dependencyGuard")
      .failsBuild()
      .taskFailed(":app:checkTakDependencies")
      .outputContainsMatch("No versions found in .*?/app/dependencies/runtimeClasspath.txt".toRegex())
  }
}
