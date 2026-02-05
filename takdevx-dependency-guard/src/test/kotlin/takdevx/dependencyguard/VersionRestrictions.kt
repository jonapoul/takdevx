package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.failsBuild
import blueprint.test.outputContains
import blueprint.test.taskFailed
import blueprint.test.taskSucceeded
import takdevx.test.PLUGIN_ID
import kotlin.test.Test

class VersionRestrictions : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    ("app" / "restrictions.txt")(
      """
        com.squareup.okio:okio:3.2.0
        com.squareup.okhttp3:okhttp:4.11.0
      """.trimIndent(),
    )

    appBuildGradleKts(
      $$"""
        plugins {
          kotlin("jvm")
          id("$$PLUGIN_ID")
        }

        takDependencyGuard {
          configuration("runtimeClasspath")
          restrictionsFile = file("restrictions.txt")
        }

        val okioVersion by properties
        val okhttpVersion by properties

        dependencies {
          implementation("com.squareup.okio:okio:$okioVersion")
          implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
          implementation("com.squareup.okhttp3-logging:okhttp:$okhttpVersion")
        }
      """.trimIndent(),
    )
  }

  @Test
  fun `Fail when dependencies aren't within restrictions`() = runScenario {
    dependencyGuardBaseline("-PokioVersion=3.16.2", "-PokhttpVersion=5.3.0")
    assertThatTask(":app:checkTakDependencies", "-PokioVersion=3.16.2", "-PokhttpVersion=5.3.0")
      .failsBuild()
      .taskFailed(":app:checkTakDependencies")
      .outputContains("com.squareup.okio:okio:3.16.2 > 3.2.0")
      .outputContains("com.squareup.okhttp3:okhttp:5.3.0 > 4.11.0")
  }

  @Test
  fun `Succeed when all dependencies are within restrictions`() = runScenario {
    dependencyGuardBaseline("-PokioVersion=3.0.0", "-PokhttpVersion=4.10.0")
    assertThatTask(":app:checkTakDependencies", "-PokioVersion=3.0.0", "-PokhttpVersion=4.10.0")
      .buildsSuccessfully()
      .taskSucceeded(":app:checkTakDependencies")
  }

  @Test
  fun `Succeed when all dependencies match restrictions`() = runScenario {
    dependencyGuardBaseline("-PokioVersion=3.2.0", "-PokhttpVersion=4.11.0")
    assertThatTask(":app:checkTakDependencies", "-PokioVersion=3.2.0", "-PokhttpVersion=4.11.0")
      .buildsSuccessfully()
      .taskSucceeded(":app:checkTakDependencies")
  }
}
