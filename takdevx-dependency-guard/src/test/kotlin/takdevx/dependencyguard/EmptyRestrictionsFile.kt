package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.failsBuild
import blueprint.test.outputContainsMatch
import blueprint.test.taskFailed
import takdevx.test.PLUGIN_ID
import kotlin.test.Test

class EmptyRestrictionsFile : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    "restrictions.txt"("")

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
          implementation("com.squareup.okio:okio:3.16.4")
        }
      """.trimIndent(),
    )
  }

  @Test
  fun `Fail when restrictions file is empty`() = runScenario {
    dependencyGuardBaseline()
    assertThatTask(":app:checkTakDependencies")
      .failsBuild()
      .taskFailed(":app:checkTakDependencies")
      .outputContainsMatch("No versions found in .*?/restrictions.txt".toRegex())
  }
}
