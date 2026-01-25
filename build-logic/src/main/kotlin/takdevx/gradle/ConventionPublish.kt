package takdevx.gradle

import com.vanniktech.maven.publish.MavenPublishPlugin
import io.github.gmazzo.publications.report.ReportPublicationsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.dokka.gradle.DokkaPlugin

class ConventionPublish : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(DokkaPlugin::class)
      apply(MavenPublishPlugin::class)
      apply(ReportPublicationsPlugin::class)
    }
  }
}
