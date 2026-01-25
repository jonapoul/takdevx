package takdevx.gradle

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPlugin
import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class TakdevxBasePlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ConventionKotlin::class)
      apply(ConventionDetekt::class)
      apply(ConventionLicensee::class)
      apply(ConventionTest::class)
      apply(DependencyGuardPlugin::class)
    }

    extensions.configure(DependencyGuardPluginExtension::class) {
      configuration("compileClasspath")
      configuration("runtimeClasspath")
    }
  }
}
