import dev.detekt.gradle.Detekt
import dev.detekt.gradle.report.ReportMergeTask

plugins {
  alias(libs.plugins.buildConfig) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.dokka) apply false
  alias(libs.plugins.kotlin) apply false
  alias(libs.plugins.kotlinAbi) apply false
  alias(libs.plugins.licensee) apply false
  alias(libs.plugins.publish) apply false
  alias(libs.plugins.publishReport) apply false

  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.dependencyGuard)
  base
}

dependencyAnalysis {
  issues {
    all {
      onAny {
        severity("fail")
      }
    }
  }
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
  val detektTasks = tasks.withType<Detekt>()
  detektReportMergeSarif.configure {
    input.from(detektTasks.map { it.reports.sarif.outputLocation })
  }
}
