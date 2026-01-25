plugins {
  id("takdevx.base")
}

dependencies {
  compileOnly(gradleApi())
  compileOnly(gradleTestKit())
  api(libs.assertk)
  api(libs.junit.api)
}
