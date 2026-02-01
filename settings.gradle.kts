rootProject.name = "takdevx"

apply(from = "gradle/repositories.gradle.kts")

pluginManagement {
  includeBuild("build-logic")
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("com.gradle.develocity") version "4.3.1"
}

develocity {
  buildScan.publishing.onlyIf { false }
}

include(
  ":takdevx-dependency-guard",
  ":takdevx-detekt",
  ":takdevx-gradle-plugin",
)

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
