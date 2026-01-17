package takdevx.dependencyguard

import assertk.assertThat
import takdevx.dependencyguard.test.PLUGIN_ID
import takdevx.dependencyguard.test.ScenarioTest
import takdevx.dependencyguard.test.outputContains
import takdevx.dependencyguard.test.runTask
import takdevx.dependencyguard.test.taskFailed
import takdevx.dependencyguard.test.taskSucceeded
import kotlin.test.Test

class VersionRestrictions : ScenarioTest() {
  override val subprojectBuildFiles = mapOf(
    "app" to $$"""
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

  override val otherFiles = mapOf(
    "app/restrictions.txt" to """
      com.squareup.okio:okio:3.2.0
      com.squareup.okhttp3:okhttp:4.11.0
    """.trimIndent(),
  )

  @Test
  fun `Fail when dependencies aren't within restrictions`() = runScenario(
    "okioVersion" to "3.16.2",
    "okhttpVersion" to "5.3.0",
  ) {
    generateBaseline()
    val result = runTask(":app:checkTakDependencies").buildAndFail()

    assertThat(result)
      .taskFailed(":app:checkTakDependencies")
      .outputContains("com.squareup.okio:okio:3.16.2 > 3.2.0")
      .outputContains("com.squareup.okhttp3:okhttp:5.3.0 > 4.11.0")
  }

  @Test
  fun `Succeed when all dependencies are within restrictions`() = runScenario(
    "okioVersion" to "3.0.0",
    "okhttpVersion" to "4.10.0",
  ) {
    generateBaseline()
    val result = runTask(":app:checkTakDependencies").build()
    assertThat(result).taskSucceeded(":app:checkTakDependencies")
  }

  @Test
  fun `Succeed when all dependencies match restrictions`() = runScenario(
    "okioVersion" to "3.2.0",
    "okhttpVersion" to "4.11.0",
  ) {
    generateBaseline()
    val result = runTask(":app:checkTakDependencies").build()
    assertThat(result).taskSucceeded(":app:checkTakDependencies")
  }
}
