package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.taskSucceeded
import takdevx.test.PLUGIN_ID
import takdevx.test.withAndroidSdk
import kotlin.test.Test

class LifecycleLibraryVersions : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    "restrictions.txt"(
      """
        androidx.lifecycle:lifecycle-common-jvm:2.9.4
        androidx.lifecycle:lifecycle-common:2.9.4
        androidx.lifecycle:lifecycle-livedata-core-ktx:2.9.4
        androidx.lifecycle:lifecycle-livedata-core:2.9.4
        androidx.lifecycle:lifecycle-runtime-android:2.9.4
        androidx.lifecycle:lifecycle-viewmodel-android:2.9.4
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

        dependencies {
          implementation("androidx.lifecycle:lifecycle-livedata-core-ktx:2.9.4")
          implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.4")
        }
      """.trimIndent(),
    )
  }

  @Test
  fun `Validate multiple lifecycle library versions matching ATAK restrictions`() = runScenario {
    dependencyGuardBaseline(withAndroidSdk = true)
    assertThatTask(":app:checkTakDependencies")
      .withAndroidSdk()
      .buildsSuccessfully()
      .taskSucceeded(":app:checkTakDependencies")
  }
}
