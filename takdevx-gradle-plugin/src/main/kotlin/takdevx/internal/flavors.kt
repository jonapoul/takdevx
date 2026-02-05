package takdevx.internal

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project

internal fun Project.registerFlavors(config: TakdevConfig) {
  pluginManager.withPlugin("com.android.application") {
    extensions.getByType(ApplicationExtension::class.java).apply {
      flavorDimensions += "application"
      productFlavors {
        val atakVersion = config.atakVersion
        val extraFlavors = config.extraFlavors.get()
        (DEFAULT_FLAVORS + extraFlavors).forEach { name ->
          register(name) { f ->
            f.dimension = "application"
            f.applicationIdSuffix = ".$name"
            if (name !in DEFAULT_FLAVORS) f.matchingFallbacks.add("civ")
            f.manifestPlaceholders["atakApiVersion"] = "com.atakmap.app@${atakVersion.get()}.${name.uppercase()}"
          }
        }
      }
    }
  }
}

private val DEFAULT_FLAVORS = setOf("civ", "mil", "gov")
