package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.taskSucceeded
import takdevx.test.PLUGIN_ID
import kotlin.test.Test

class PreReleaseVersionHandling : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    "restrictions.txt"(
      """
        androidx.core:core:1.17.0
        androidx.fragment:fragment:1.8.9
      """.trimIndent(),
    )

    ("app" / "dependencies" / "runtimeClasspath.txt")(
      """
        androidx.core:core:1.17.0-alpha01
        androidx.fragment:fragment:1.8.0-beta02
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
      """.trimIndent(),
    )
  }

  @Test
  fun `Pre-release versions should be less than stable versions`() = runScenario {
    assertThatTask(":app:checkTakDependencies", "-x", "dependencyGuard")
      .buildsSuccessfully()
      .taskSucceeded(":app:checkTakDependencies")
  }
}
