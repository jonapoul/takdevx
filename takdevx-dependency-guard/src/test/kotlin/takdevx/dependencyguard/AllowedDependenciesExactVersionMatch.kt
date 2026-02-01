package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.failsBuild
import blueprint.test.outputContains
import blueprint.test.taskFailed
import takdevx.test.PLUGIN_ID
import kotlin.test.Test

class AllowedDependenciesExactVersionMatch : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    "restrictions.txt"("androidx.core:core:1.15.0")

    "gradle.properties"("android.useAndroidX=true")

    appBuildGradleKts(
      """
        plugins {
          kotlin("android")
          id("com.android.library")
          id("$PLUGIN_ID")
        }

        android {
          namespace = "com.example.app"
          compileSdk = 36
        }

        takDependencyGuard {
          configuration("debugRuntimeClasspath")
          restrictionsFile = rootProject.file("restrictions.txt")

          // Allow version 1.16.0 explicitly - version 1.17.0 should NOT be allowed
          allow("androidx.core", "core", "1.16.0")
        }

        dependencies {
          implementation("androidx.core:core:1.17.0")
        }
      """.trimIndent(),
    )
  }

  @Test
  fun `Different version of same dependency is not allowed`() = runScenario {
    // Version 1.19.0 is NOT in the allowlist (only 1.18.0 is)
    // So the task should fail even though the same dependency with different version is allowed
    dependencyGuardBaseline(withAndroidSdk = true)
    assertThatTask(":app:checkTakDependencies")
      .withAndroidSdk()
      .failsBuild()
      .taskFailed(":app:checkTakDependencies")
      .outputContains("androidx.core:core:1.17.0 > 1.15.0")
  }
}
