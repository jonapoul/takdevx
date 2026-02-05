package takdevx.test

import blueprint.test.DEFAULT_REPOSITORIES_KTS
import blueprint.test.FileTree
import blueprint.test.ScenarioTest
import org.intellij.lang.annotations.Language

public abstract class BasicScenarioTest : ScenarioTest() {
  public abstract val pluginId: String
  override val gradleVersion: String = GRADLE_VERSION

  protected fun FileTree.Builder.settingsGradleKts(): Unit = "settings.gradle.kts"(
    """
      $DEFAULT_REPOSITORIES_KTS
      include(":app")
    """.trimIndent(),
  )

  protected fun FileTree.Builder.rootBuildGradleKts(): Unit = "build.gradle.kts"(
    """
      plugins {
        kotlin("jvm") apply false
        kotlin("android") apply false
        id("$pluginId") apply false
      }
    """.trimIndent(),
  )

  protected fun FileTree.Builder.libsVersionsToml(
    @Language("toml") content: String,
  ): Unit = ("gradle" / "libs.versions.toml")(content)

  protected fun FileTree.Builder.appBuildGradleKts(
    @Language("kotlin") content: String,
  ): Unit = ("app" / "build.gradle.kts")(content)
}
