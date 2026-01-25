package takdevx.dependencyguard

import assertk.assertThat
import takdevx.test.PLUGIN_ID
import takdevx.test.outputContains
import takdevx.test.runTask
import takdevx.test.taskFailed
import takdevx.test.taskSucceeded
import kotlin.test.Test

class TwoPartVersionNumbers : DependencyGuardScenarioTest() {
  override val rootBuildFile: String = """
    plugins {
      kotlin("android") apply false
      id("com.android.application") apply false
      id("$PLUGIN_ID") apply false
    }
  """.trimIndent()

  override val subprojectBuildFiles = mapOf(
    "app" to $$"""
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
        restrictionsFile = file("restrictions.txt")
      }

      val androidSvgVersion by properties

      dependencies {
        implementation("com.caverock:androidsvg-aar:$androidSvgVersion")
      }
    """.trimIndent(),
  )

  override val otherFiles = mapOf(
    "app/restrictions.txt" to """
      com.caverock:androidsvg-aar:1.3
    """.trimIndent(),
  )

  @Test
  fun `Fail when two-part version exceeds restriction`() = runScenario("androidSvgVersion" to "1.4") {
    generateBaseline()
    val result = runTask(":app:checkTakDependencies", requiresAndroid = true).buildAndFail()

    assertThat(result)
      .taskFailed(":app:checkTakDependencies")
      .outputContains("com.caverock:androidsvg-aar:1.4 > 1.3")
  }

  @Test
  fun `Succeed when two-part version is within restriction`() = runScenario("androidSvgVersion" to "1.3") {
    generateBaseline()
    val result = runTask(":app:checkTakDependencies", requiresAndroid = true).build()
    assertThat(result).taskSucceeded(":app:checkTakDependencies")
  }
}
