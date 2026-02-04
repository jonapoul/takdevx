package takdevx.gradle

import kotlinx.validation.BinaryCompatibilityValidatorPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ConventionKotlin : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(KotlinPluginWrapper::class)
      apply(BinaryCompatibilityValidatorPlugin::class)
    }

    val javaVersion = providers.gradleProperty("takdevx.javaVersion")

    extensions.configure(KotlinJvmProjectExtension::class) {
      explicitApi()

      compilerOptions {
        allWarningsAsErrors.set(true)
        jvmTarget.set(javaVersion.map(JvmTarget::fromTarget))
        freeCompilerArgs.addAll(
          "-opt-in=kotlin.RequiresOptIn",
          "-Xmulti-dollar-interpolation",
          "-Xcontext-receivers", // context-parameters isn't supported by this kotlin version
        )
      }
    }

    extensions.configure(JavaPluginExtension::class) {
      val version = javaVersion.map(JavaVersion::toVersion).get()
      sourceCompatibility = version
      targetCompatibility = version
    }

    val compileTasks = tasks.withType(KotlinCompile::class.java)
    tasks.register("compileAll") { dependsOn(compileTasks) }
  }
}
