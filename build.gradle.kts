plugins {
  alias(libs.plugins.dependencyGuard)
}

dependencyGuard {
  configuration("classpath")
}
