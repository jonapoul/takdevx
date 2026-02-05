package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.taskSucceeded
import takdevx.test.PLUGIN_ID
import kotlin.test.Test

class AllowedDependenciesStringFormat : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    "restrictions.txt"("com.squareup.okio:okio:3.1.0")

    appBuildGradleKts(
      """
        plugins {
          kotlin("jvm")
          id("$PLUGIN_ID")
        }

        takDependencyGuard {
          configuration("runtimeClasspath")
          restrictionsFile = rootProject.file("restrictions.txt")

          // Test string format: allow("group:artifact:version")
          allow("com.squareup.okio:okio:3.16.4")
        }

        dependencies { implementation("com.squareup.okio:okio:3.16.4") }
      """.trimIndent(),
    )
  }

  @Test
  fun `String coordinate format allows dependency to bypass restriction`() = runScenario {
    assertThatTask(":app:checkTakDependencies")
      .buildsSuccessfully()
      .taskSucceeded(":app:checkTakDependencies")
  }
}
