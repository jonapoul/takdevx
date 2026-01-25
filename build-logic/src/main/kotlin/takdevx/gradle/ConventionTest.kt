package takdevx.gradle

import blueprint.core.get
import blueprint.core.libs
import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.buildConfigField
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.withType
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata
import org.gradle.util.GradleVersion
import java.io.File

class ConventionTest : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(BuildConfigPlugin::class)
    }

    tasks.withType(Test::class).configureEach {
      useJUnitPlatform()
      testLogging {
        events = setOf(PASSED, SKIPPED, FAILED)
        exceptionFormat = FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
        showStandardStreams = true
        displayGranularity = 2
      }
    }

    pluginManager.withPlugin("takdevx.convention.gradle") {
      extensions.configure(BuildConfigExtension::class) {
        generateAtSync.set(true)
        sourceSets.getByName("test") {
          packageName.set("takdevx.test")
          useKotlinOutput { topLevelConstants = true }
          buildConfigField("GRADLE_VERSION", GradleVersion.current().version)
          buildConfigField("PLUGIN_ID", target.pluginId)
          buildConfigField<File?>("ANDROID_HOME", androidHome())
        }
      }

      val testPluginClasspath by configurations.registering { isCanBeResolved = true }

      tasks.withType(PluginUnderTestMetadata::class).configureEach {
        pluginClasspath.from(testPluginClasspath)
      }
    }

    dependencies {
      "testImplementation"(kotlin("stdlib"))
      "testImplementation"(kotlin("test"))
      "testImplementation"(libs["assertk"])
      "testImplementation"(libs["junit.api"])
      "testImplementation"(libs["junit.params"])
      "testRuntimeOnly"(libs["junit.launcher"])
    }
  }
}
