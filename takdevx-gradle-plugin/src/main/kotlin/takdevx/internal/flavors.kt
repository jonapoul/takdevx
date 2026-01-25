package takdevx.internal

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project

internal fun Project.registerFlavors() {
  val atakVersion = providers.gradleProperty("ATAK_VERSION")
  val allFlavors = providers.gradleProperty("takdevx.extraFlavors").map { str ->
    setOf("civ", "mil", "gov") + str.split(",").map { it.trim().lowercase() }
  }

  pluginManager.withPlugin("com.android.application") {
    extensions.getByType(ApplicationExtension::class.java).productFlavors {
      allFlavors.get().forEach { name ->
        register(name) { flavor ->
          flavor.dimension = "application"
          flavor.applicationIdSuffix = ".$name"
          flavor.matchingFallbacks.add("civ")
          flavor.manifestPlaceholders["atakApiVersion"] = "com.atakmap.app@${atakVersion.get()}.${name.uppercase()}"
        }
      }
    }
  }
}
