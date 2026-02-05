package takdevx

import blueprint.test.DEFAULT_REPOSITORIES_KTS
import blueprint.test.assertThatTask
import blueprint.test.buildsSuccessfully
import blueprint.test.outputContains
import takdevx.test.TakdevxGradleScenarioTest
import takdevx.test.withAndroidSdk
import kotlin.test.Ignore
import kotlin.test.Test

class TakdevxBasicTest : TakdevxGradleScenarioTest() {
  override val fileTree = fileTree {
    "settings.gradle.kts"(
      """
        $DEFAULT_REPOSITORIES_KTS

        plugins {
          id("dev.jonpoulton.takdevx.settings")
        }

        include(":app")
      """.trimIndent(),
    )

    "build.gradle.kts"(
      """
        plugins {
          kotlin("android") apply false
          id("com.android.application") apply false
          id("dev.jonpoulton.takdevx") apply false
        }
      """.trimIndent(),
    )

    ("sdk" / "main.jar")("This is a dummy JAR file")

    "gradle.properties"(
      """
        ATAK_VERSION=5.6.0
      """.trimIndent(),
    )

    "app" {
      "build.gradle.kts"(
        """
          plugins {
            kotlin("android")
            id("com.android.application")
            id("dev.jonpoulton.takdevx")
          }

          android {
            namespace = "dev.jonpoulton.tak.example"
            compileSdk = 36
          }
        """.trimIndent(),
      )
    }
  }

  @Test
  @Ignore // wait until it works
  fun `App flavours added`() = runScenario {
    val sdkPath = rootDir.resolve("sdk")
    assertThatTask(":app:tasks", "-PsdkPath=${sdkPath.absolutePath}")
      .withAndroidSdk()
      .buildsSuccessfully()
      .outputContains("srgmjseg")
  }
}
