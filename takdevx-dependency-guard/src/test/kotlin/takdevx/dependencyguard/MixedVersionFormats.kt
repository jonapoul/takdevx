package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.taskSucceeded
import takdevx.test.PLUGIN_ID
import kotlin.test.Test

class MixedVersionFormats : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    "restrictions.txt"(
      """
        com.caverock:androidsvg-aar:1.4
        androidx.core:core:1.17.0
        gov.tak.thirdparty:libLAS:1.8.2i
        org.jetbrains.kotlin:kotlin-stdlib:2.2.0
        com.google.guava:listenablefuture:1.0
      """.trimIndent(),
    )

    ("app" / "dependencies" / "runtimeClasspath.txt")(
      """
        com.caverock:androidsvg-aar:1.4
        androidx.core:core:1.17.0
        gov.tak.thirdparty:libLAS:1.8.2i
        org.jetbrains.kotlin:kotlin-stdlib:2.2.0
        com.google.guava:listenablefuture:1.0
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
  fun `Handle mixed version formats from real ATAK restrictions`() = runScenario {
    assertThatTask(":app:checkTakDependencies", "-x", "dependencyGuard")
      .buildsSuccessfully()
      .taskSucceeded(":app:checkTakDependencies")
  }
}
