package takdevx.gradle

import app.cash.licensee.LicenseeExtension
import app.cash.licensee.LicenseePlugin
import app.cash.licensee.UnusedAction.IGNORE
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class ConventionLicensee : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(LicenseePlugin::class)
    }

    extensions.configure(LicenseeExtension::class) {
      allow("Apache-2.0")
      allow("EPL-2.0")
      unusedAction(IGNORE)
    }
  }
}
