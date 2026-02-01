package takdevx.dependencyguard

import assertk.Assert
import blueprint.test.DEFAULT_REPOSITORIES_KTS
import blueprint.test.FileTree
import blueprint.test.Scenario
import blueprint.test.ScenarioTest
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import org.gradle.testkit.runner.BuildResult
import org.intellij.lang.annotations.Language
import takdevx.test.GRADLE_VERSION
import takdevx.test.PLUGIN_ID
import java.io.File

abstract class DependencyGuardScenarioTest : ScenarioTest() {
  override val gradleVersion = GRADLE_VERSION

  protected fun deleteCachedRestrictions() {
    System
      .getProperty("user.home")
      .let(::File)
      .resolve(".gradle")
      .resolve("caches")
      .resolve("takdevx")
      .resolve("tak-dependency-guard")
      .deleteRecursively()
  }

  protected fun Scenario.dependencyGuardBaseline(
    vararg args: String,
    withAndroidSdk: Boolean = false,
  ): Assert<BuildResult> = assertThatTask("dependencyGuardBaseline", *args)
    .apply { if (withAndroidSdk) withAndroidSdk() }
    .buildsSuccessfully()

  protected fun FileTree.Builder.settingsGradleKts() = "settings.gradle.kts"(
    """
      $DEFAULT_REPOSITORIES_KTS
      include(":app")
    """.trimIndent(),
  )

  protected fun FileTree.Builder.rootBuildGradleKts() = "build.gradle.kts"(
    """
      plugins {
        kotlin("jvm") apply false
        kotlin("android") apply false
        id("$PLUGIN_ID") apply false
      }
    """.trimIndent(),
  )

  protected fun FileTree.Builder.libsVersionsToml(
    @Language("toml") content: String,
  ) = ("gradle" / "libs.versions.toml")(content)

  protected fun FileTree.Builder.appBuildGradleKts(
    @Language("kotlin") content: String,
  ) = ("app" / "build.gradle.kts")(content)
}
