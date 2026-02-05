package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.failsBuild
import blueprint.test.outputContains
import blueprint.test.taskFailed
import blueprint.test.taskSucceeded
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import takdevx.test.PLUGIN_ID
import kotlin.test.Ignore
import kotlin.test.Test

/**
 * This test class will actually download the file from GitHub
 */
class SetAtakVersionAndDownloadRestrictions : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    "gradle.properties"("ATAK_VERSION=5.6.0")

    appBuildGradleKts(
      $$"""
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
  }

  @BeforeEach fun before() = deleteCachedRestrictions()

  @AfterEach fun after() = deleteCachedRestrictions()

  @Test
  @Ignore
  fun `Fail when dependencies aren't within restrictions`() = runScenario {
    dependencyGuardBaseline("-PokioVersion=3.16.2", "-PokhttpVersion=5.3.0")
    assertThatTask(":app:checkTakDependencies", "-PokioVersion=3.16.2", "-PokhttpVersion=5.3.0")
      .failsBuild()
      .taskSucceeded(":downloadTakDependencies")
      .taskFailed(":app:checkTakDependencies")
      .outputContains("com.squareup.okio:okio:3.16.2 > 3.2.0")
      .outputContains("com.squareup.okhttp3:okhttp:5.3.0 > 4.11.0")
      .outputContains("org.jetbrains.kotlin:kotlin-stdlib:2.2.21 > 2.2.0")
  }

  @Test
  @Ignore
  fun `Succeed when all dependencies are within restrictions`() = runScenario {
    dependencyGuardBaseline("-PokioVersion=3.0.0", "-PokhttpVersion=4.10.0")
    assertThatTask(":app:checkTakDependencies", "-PokioVersion=3.0.0", "-PokhttpVersion=4.10.0")
      .buildsSuccessfully()
      .taskSucceeded(":downloadTakDependencies")
      .taskSucceeded(":app:checkTakDependencies")
  }
}
