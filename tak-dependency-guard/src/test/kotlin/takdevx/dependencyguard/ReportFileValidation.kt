package takdevx.dependencyguard

import assertk.assertThat
import assertk.assertions.exists
import assertk.assertions.isNotEmpty
import assertk.assertions.isTrue
import takdevx.dependencyguard.test.PLUGIN_ID
import takdevx.dependencyguard.test.ScenarioTest
import takdevx.dependencyguard.test.contains
import takdevx.dependencyguard.test.doesNotContain
import takdevx.dependencyguard.test.runTask
import takdevx.dependencyguard.test.taskFailed
import kotlin.test.Test

class ReportFileValidation : ScenarioTest() {
  override val subprojectBuildFiles = mapOf(
    "app" to """
      plugins {
        kotlin("jvm")
        id("$PLUGIN_ID")
      }

      takDependencyGuard {
        configuration("runtimeClasspath")
        configuration("testRuntimeClasspath")
        restrictionsFile = rootProject.file("restrictions.txt")
      }
    """.trimIndent(),
  )

  override val otherFiles = mapOf(
    // Baseline file for runtime dependencies
    "app/dependencies/runtimeClasspath.txt" to """
      androidx.core:core:1.18.0
      androidx.fragment:fragment:1.9.5
      androidx.appcompat:appcompat:1.6.0
    """.trimIndent(),

    // Baseline file for test runtime dependencies
    "app/dependencies/testRuntimeClasspath.txt" to """
      junit:junit:4.13.2
      org.junit.jupiter:junit-jupiter:5.10.0
    """.trimIndent(),

    // Restrictions file with maximum allowed versions
    "restrictions.txt" to """
      androidx.core:core:1.17.0
      androidx.fragment:fragment:1.8.9
      androidx.appcompat:appcompat:1.7.0
      junit:junit:4.13.2
      org.junit.jupiter:junit-jupiter:5.9.0
    """.trimIndent(),
  )

  @Test
  fun `Report file is created with properly formatted violations`() = runScenario {
    assertThat(runTask(":app:checkTakDependencies").buildAndFail())
      .taskFailed(":app:checkTakDependencies")

    val reportFile = resolve("app/build/reports/takdevx/tak-dependency-guard.txt")
    assertThat(reportFile).exists()

    val reportContent = reportFile.readText()
    assertThat(reportContent).isNotEmpty()
    assertThat(reportContent)
      .contains("runtimeClasspath")
      .contains("testRuntimeClasspath")
      .contains("androidx.core:core:1.18.0 > 1.17.0")
      .contains("androidx.fragment:fragment:1.9.5 > 1.8.9")
      .contains("org.junit.jupiter:junit-jupiter:5.10.0 > 5.9.0")
      .doesNotContain("androidx.appcompat:appcompat")
      .doesNotContain("junit:junit")
  }

  @Test
  fun `Report file format matches expected structure`() = runScenario {
    runTask(":app:checkTakDependencies").buildAndFail()

    val reportFile = resolve("app/build/reports/takdevx/tak-dependency-guard.txt")
    val reportLines = reportFile.readLines()

    // Verify report structure:
    // - Configuration name (no indentation)
    // - Violation lines (indented with 2 spaces)
    // - Empty line separator

    var foundRuntimeClasspath = false
    var foundViolationAfterRuntime = false

    reportLines.forEach { line ->
      if (line == "runtimeClasspath") {
        foundRuntimeClasspath = true
      }
      if (foundRuntimeClasspath && line.startsWith("  ") && line.contains(">")) {
        foundViolationAfterRuntime = true
      }
    }

    assertThat(foundRuntimeClasspath).isTrue()
    assertThat(foundViolationAfterRuntime).isTrue()
  }
}
