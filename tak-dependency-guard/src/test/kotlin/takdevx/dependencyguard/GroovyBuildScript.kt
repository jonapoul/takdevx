package takdevx.dependencyguard

import assertk.assertThat
import takdevx.dependencyguard.test.PLUGIN_ID
import takdevx.dependencyguard.test.ScenarioTest
import takdevx.dependencyguard.test.runTask
import takdevx.dependencyguard.test.taskSucceeded
import kotlin.test.Test

class GroovyBuildScript : ScenarioTest() {
  override val isGroovy = true

  override val rootBuildFile = """
    plugins {
      id 'org.jetbrains.kotlin.jvm' apply false
      id '$PLUGIN_ID' apply false
    }
  """.trimIndent()

  override val subprojectBuildFiles = mapOf(
    "app" to """
      plugins {
        id 'org.jetbrains.kotlin.jvm'
        id '$PLUGIN_ID'
      }

      takDependencyGuard {
        configuration('runtimeClasspath')
        restrictionsFile = rootProject.file('restrictions.txt')
      }
    """.trimIndent(),
  )

  override val otherFiles = mapOf(
    "app/dependencies/runtimeClasspath.txt" to """
      androidx.core:core:1.15.0
    """.trimIndent(),

    "restrictions.txt" to """
      androidx.core:core:1.17.0
    """.trimIndent(),
  )

  @Test
  fun `Plugin works with Groovy build scripts`() = runScenario {
    val result = runTask(":app:checkTakDependencies").build()

    assertThat(result)
      .taskSucceeded(":app:checkTakDependencies")
  }
}
