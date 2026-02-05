package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.failsBuild
import blueprint.test.outputContains
import blueprint.test.outputDoesNotContain
import blueprint.test.taskFailed
import blueprint.test.taskSucceeded
import takdevx.test.PLUGIN_ID
import takdevx.test.withAndroidSdk
import kotlin.test.Test

class AllowedDependenciesBasic : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    "gradle.properties"("android.useAndroidX=true")

    "restrictions.txt"(
      """
        androidx.core:core:1.16.0
        androidx.fragment:fragment:1.8.8
      """.trimIndent(),
    )
    appBuildGradleKts(
      """
        plugins {
          kotlin("android")
          id("com.android.library")
          id("$PLUGIN_ID")
        }

        android {
          namespace = "com.example"
          compileSdk = 36
        }

        val allowFragment = properties["allowFragment"] == "true"

        takDependencyGuard {
          configuration("debugRuntimeClasspath")
          restrictionsFile = rootProject.file("restrictions.txt")

          allow("androidx.core", "core", "1.17.0")
          if (allowFragment) {
            allow("androidx.fragment", "fragment", "1.8.9")
          }
        }

        dependencies {
          implementation("androidx.core:core:1.17.0")
          implementation("androidx.fragment:fragment:1.8.9")
        }
      """.trimIndent(),
    )
  }

  @Test
  fun `Allowed dependency bypasses restriction while non-allowed dependency still fails`() = runScenario {
    dependencyGuardBaseline(withAndroidSdk = true)
    assertThatTask(":app:checkTakDependencies")
      .withAndroidSdk()
      .failsBuild()
      .taskFailed(":app:checkTakDependencies")
      .outputContains("androidx.fragment:fragment:1.8.9 > 1.8.8")
      .outputDoesNotContain("androidx.core:core:1.17.0")
  }

  @Test
  fun `All dependencies pass when all violators are in allowlist`() = runScenario {
    dependencyGuardBaseline(withAndroidSdk = true)
    assertThatTask(":app:checkTakDependencies", "-PallowFragment=true")
      .withAndroidSdk()
      .buildsSuccessfully()
      .taskSucceeded(":app:checkTakDependencies")
  }
}
