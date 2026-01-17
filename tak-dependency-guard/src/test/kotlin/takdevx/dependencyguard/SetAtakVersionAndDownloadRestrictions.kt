package takdevx.dependencyguard

import assertk.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import takdevx.dependencyguard.test.PLUGIN_ID
import takdevx.dependencyguard.test.ScenarioTest
import takdevx.dependencyguard.test.outputContains
import takdevx.dependencyguard.test.runTask
import takdevx.dependencyguard.test.taskFailed
import takdevx.dependencyguard.test.taskSucceeded
import kotlin.test.Test

/**
 * This test class will actually download the file from GitHub
 */
class SetAtakVersionAndDownloadRestrictions : ScenarioTest() {
  override val gradlePropertiesFile = """
    ATAK_VERSION=5.6.0
  """.trimIndent()

  override val subprojectBuildFiles = mapOf(
    "app" to $$"""
      plugins {
        kotlin("jvm")
        id("$$PLUGIN_ID")
      }

      takDependencyGuard {
        configuration("runtimeClasspath")
      }

      val okioVersion by properties
      val okhttpVersion by properties

      dependencies {
        implementation("com.squareup.okio:okio:$okioVersion")
        implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
      }
    """.trimIndent(),
  )

  @BeforeEach fun before() = deleteCachedRestrictions()

  @AfterEach fun after() = deleteCachedRestrictions()

  @Test
  fun `Fail when dependencies aren't within restrictions`() = runScenario(
    "okioVersion" to "3.16.2",
    "okhttpVersion" to "5.3.0",
  ) {
    generateBaseline()
    val result = runTask(":app:checkTakDependencies").buildAndFail()

    assertThat(result)
      .taskSucceeded(":app:downloadTakDependencies")
      .taskFailed(":app:checkTakDependencies")
      .outputContains("com.squareup.okio:okio:3.16.2 > 3.2.0")
      .outputContains("com.squareup.okhttp3:okhttp:5.3.0 > 4.11.0")
      .outputContains("org.jetbrains.kotlin:kotlin-stdlib:2.2.21 > 2.2.0")
  }

  @Test
  fun `Succeed when all dependencies are within restrictions`() = runScenario(
    "okioVersion" to "3.0.0",
    "okhttpVersion" to "4.10.0",
  ) {
    generateBaseline()
    val result = runTask(":app:checkTakDependencies").build()
    assertThat(result)
      .taskSucceeded(":app:downloadTakDependencies")
      .taskSucceeded(":app:checkTakDependencies")
  }
}
