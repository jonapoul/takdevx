package takdevx.dependencyguard

import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.failsBuild
import blueprint.test.outputContains
import blueprint.test.taskFailed
import blueprint.test.taskSucceeded
import takdevx.test.PLUGIN_ID
import kotlin.test.Test

class GroovyBuildScript : DependencyGuardScenarioTest() {
  override val fileTree = fileTree {
    "settings.gradle"(
      """
        pluginManagement {
          repositories {
            mavenCentral()
            google()
            gradlePluginPortal()
          }
        }

        dependencyResolutionManagement {
          repositories {
            google()
            mavenCentral()
          }
        }

        include ":app"
      """.trimIndent()
    )

    "build.gradle"(
      """
        plugins {
          id 'org.jetbrains.kotlin.jvm' apply false
          id '$PLUGIN_ID' apply false
        }
      """.trimIndent()
    )

    "restrictions.txt"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    ("app" / "build.gradle")(
      $$"""
        plugins {
          id 'org.jetbrains.kotlin.jvm'
          id '$$PLUGIN_ID'
        }

        takDependencyGuard {
          configuration 'runtimeClasspath'
          restrictionsFile = rootProject.file('restrictions.txt')
        }

        def coroutinesVersion = project.property('coroutinesVersion')
        dependencies {
          implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
        }
      """.trimIndent(),
    )
  }

  @Test
  fun `Plugin works with Groovy build scripts`() = runScenario {
    dependencyGuardBaseline("-PcoroutinesVersion=1.8.1")
    assertThatTask(":app:checkTakDependencies", "-PcoroutinesVersion=1.8.1")
      .buildsSuccessfully()
      .taskSucceeded(":app:checkTakDependencies")
  }

  @Test
  fun `Plugin fails as expected with Groovy build scripts`() = runScenario {
    dependencyGuardBaseline("-PcoroutinesVersion=1.9.0")
    assertThatTask(":app:checkTakDependencies", "-PcoroutinesVersion=1.9.0")
      .failsBuild()
      .taskFailed(":app:checkTakDependencies")
      .outputContains("Failed TAK dependency validations")
      .outputContains("runtimeClasspath")
      .outputContains("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0 > 1.8.1")
  }
}
