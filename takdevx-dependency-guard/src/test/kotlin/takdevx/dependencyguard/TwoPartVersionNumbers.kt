package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.failsBuild
import blueprint.test.outputContains
import blueprint.test.taskFailed
import blueprint.test.taskSucceeded
import takdevx.test.PLUGIN_ID
import takdevx.test.withAndroidSdk
import kotlin.test.Test

class TwoPartVersionNumbers : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    "build.gradle.kts"(
      """
        plugins {
          kotlin("android") apply false
          id("com.android.application") apply false
          id("$PLUGIN_ID") apply false
        }
      """.trimIndent(),
    )

    "restrictions.txt"("com.caverock:androidsvg-aar:1.3")

    appBuildGradleKts(
      $$"""
        plugins {
          kotlin("android")
          id("com.android.application")
          id("$$PLUGIN_ID")
        }

        android {
          namespace = "com.example.app"
          compileSdk = 36

          defaultConfig {
            minSdk = 21
            targetSdk = 36
          }
        }

        takDependencyGuard {
          configuration("releaseRuntimeClasspath")
          restrictionsFile = rootProject.file("restrictions.txt")
        }

        val androidSvgVersion by properties
        dependencies {
          implementation("com.caverock:androidsvg-aar:$androidSvgVersion")
        }
      """.trimIndent(),
    )
  }

  @Test
  fun `Fail when two-part version exceeds restriction`() = runScenario {
    dependencyGuardBaseline("-PandroidSvgVersion=1.4", withAndroidSdk = true)
    assertThatTask(":app:checkTakDependencies", "-PandroidSvgVersion=1.4")
      .withAndroidSdk()
      .failsBuild()
      .taskFailed(":app:checkTakDependencies")
      .outputContains("com.caverock:androidsvg-aar:1.4 > 1.3")
  }

  @Test
  fun `Succeed when two-part version is within restriction`() = runScenario {
    dependencyGuardBaseline("-PandroidSvgVersion=1.3", withAndroidSdk = true)
    assertThatTask(":app:checkTakDependencies", "-PandroidSvgVersion=1.3")
      .withAndroidSdk()
      .buildsSuccessfully()
      .taskSucceeded(":app:checkTakDependencies")
  }
}
