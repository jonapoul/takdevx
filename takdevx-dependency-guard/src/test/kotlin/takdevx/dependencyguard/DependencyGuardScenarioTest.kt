package takdevx.dependencyguard

import assertk.Assert
import blueprint.test.Scenario
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import org.gradle.testkit.runner.BuildResult
import takdevx.test.BasicScenarioTest
import takdevx.test.PLUGIN_ID
import takdevx.test.withAndroidSdk
import java.io.File

abstract class DependencyGuardScenarioTest : BasicScenarioTest() {
  override val pluginId: String = PLUGIN_ID

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
}
