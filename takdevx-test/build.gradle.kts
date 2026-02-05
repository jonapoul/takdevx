import takdevx.gradle.androidHome

plugins {
  id("takdevx.base")
  alias(libs.plugins.buildConfig)
}

buildConfig {
  generateAtSync = true
  packageName = "takdevx.test"
  useKotlinOutput { topLevelConstants = true }
  buildConfigField("GRADLE_VERSION", GradleVersion.current().version)
  buildConfigField<File?>("ANDROID_HOME", androidHome())
}

dependencies {
  compileOnly(gradleTestKit())
  api(kotlin("stdlib"))
  api(kotlin("test"))
  api(libs.assertk)
  api(libs.blueprint.assertk)
  api(libs.junit.api)
  api(libs.junit.params)
}
