@file:Suppress("UnstableApiUsage")

rootProject.name = "takdevx"

pluginManagement {
  includeBuild("build-logic")
  repositories {
    google {
      mavenContent {
        includeGroupByRegex(".*android.*")
        includeGroupByRegex(".*google.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    mavenLocal()
  }
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
  ":takdevx-test",
)

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
