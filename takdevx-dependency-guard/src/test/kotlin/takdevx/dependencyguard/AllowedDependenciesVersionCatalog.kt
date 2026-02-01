package takdevx.dependencyguard

import assertk.assertThat
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.taskSucceeded
import takdevx.test.PLUGIN_ID
import java.io.File
import kotlin.test.Test

class AllowedDependenciesVersionCatalog : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    "restrictions.txt"("com.squareup.okhttp3:okhttp:4.11.0")

    appBuildGradleKts(
      """
        plugins {
          kotlin("jvm")
          id("$PLUGIN_ID")
        }

        takDependencyGuard {
          configuration("runtimeClasspath")
          restrictionsFile = rootProject.file("restrictions.txt")

          // Use version catalog reference
          allow(libs.okhttp)
        }

        dependencies {
          implementation(libs.okhttp)
        }
      """.trimIndent(),
    )

    libsVersionsToml(
      """
        [libraries]
        okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version = "5.3.0" }
      """.trimIndent(),
    )
  }

  @Test
  fun `Version catalog reference allows dependency to bypass restriction`() = runScenario {
    dependencyGuardBaseline()

    assertThat(rootDir.resolve("app/dependencies/runtimeClasspath.txt"))
      .transform(transform = File::readText)
      .contains("com.squareup.okhttp3:okhttp:5.3.0")

    assertThatTask(":app:checkTakDependencies")
      .buildsSuccessfully()
      .taskSucceeded(":app:checkTakDependencies")
  }
}
