import dev.detekt.gradle.Detekt
import dev.detekt.gradle.report.ReportMergeTask

plugins {
  alias(libs.plugins.buildConfig) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.dokka) apply false
  alias(libs.plugins.kotlin) apply false
  alias(libs.plugins.kotlinAbi) apply false
  alias(libs.plugins.publish) apply false
  alias(libs.plugins.publishReport) apply false

  alias(libs.plugins.dependencyGuard)
  base
}

dependencyGuard {
  configuration("classpath")
}

val detektReportMergeSarif by tasks.registering(ReportMergeTask::class) {
  output = layout.buildDirectory.file("reports/detekt/merge.sarif.json")
}

tasks.check.configure {
  dependsOn(detektReportMergeSarif)
}

allprojects {
  detektReportMergeSarif.configure {
    input.from(tasks.withType<Detekt>().map { it.reports.sarif.outputLocation })
  }
}
