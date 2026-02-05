plugins {
  id("takdevx.base")
  id("takdevx.convention.publish")
}

dependencies {
  compileOnly(libs.detekt.api)
  testImplementation(kotlin("scripting-jvm"))
  testImplementation(libs.detekt.test)
  testImplementation(libs.detekt.testJunit)
  testImplementation(libs.detekt.testUtils)
}
