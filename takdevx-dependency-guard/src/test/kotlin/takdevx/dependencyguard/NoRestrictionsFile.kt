package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.failsBuild
import blueprint.test.outputContainsMatch
import takdevx.test.PLUGIN_ID
import kotlin.test.Test

class NoRestrictionsFile : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    settingsGradleKts()
    rootBuildGradleKts()

    ("app" / "dependencies" / "runtimeClasspath.txt")("a.b.c:x.y.z:1.2.3")

    appBuildGradleKts(
      """
        plugins {
          kotlin("jvm")
          id("$PLUGIN_ID")
        }

        takDependencyGuard {
          configuration("runtimeClasspath")
          restrictionsFile = rootProject.file("some-other-file.txt")
        }
      """.trimIndent(),
    )
  }

  @Test
  fun `Fail if restrictions file doesn't exist`() = runScenario {
    assertThatTask(":app:checkTakDependencies", "-x", "dependencyGuard")
      .failsBuild()
      .outputContainsMatch("'restrictionsFile' specifies file '.*?/some-other-file.txt' which doesn't exist.".toRegex())
  }
}
