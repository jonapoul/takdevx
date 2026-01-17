package takdevx.dependencyguard.internal

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import takdevx.dependencyguard.CheckDependencies
import takdevx.dependencyguard.DownloadFile
import takdevx.dependencyguard.TakDependencyGuardExtension

private const val BASE_URL = "https://raw.githubusercontent.com/jonapoul/takdevx/main/atak-versions/"

internal fun Project.registerDownloadFileTask(
  extension: TakDependencyGuardExtension,
): TaskProvider<DownloadFile> {
  val filename = extension
    .atakVersion
    .orElse(atakVersionProperty)
    .map { "restrictions-$it.txt" }

  // Use restrictionsUrl if set, otherwise construct URL from atakVersion
  val downloadUrl = extension
    .restrictionsUrl
    .orElse(filename.map { "$BASE_URL/$it" })

  val cacheDir = gradle.gradleUserHomeDir
    .resolve("caches")
    .resolve("takdevx")
    .resolve("tak-dependency-guard")

  val restrictionsFile = extension.restrictionsFile

  return tasks.register("downloadTakDependencies", DownloadFile::class.java) { t ->
    t.url.set(downloadUrl)
    t.destinationFile.set(cacheDir.resolve("restrictions.txt"))
    t.onlyIf { !restrictionsFile.isPresent }
  }
}

internal fun Project.registerCheckTakDependenciesTask(
  extension: TakDependencyGuardExtension,
  downloadFile: TaskProvider<DownloadFile>,
): TaskProvider<CheckDependencies> {
  val restrictionsFile = extension.restrictionsFile.orElse(downloadFile.flatMap { it.destinationFile })
  return tasks.register("checkTakDependencies", CheckDependencies::class.java) { t ->
    t.restrictionsFile.set(restrictionsFile)
    t.guardFileDir.set(layout.projectDirectory.dir("dependencies"))
    t.reportFile.set(layout.buildDirectory.file("reports/tak-dependency-guard.txt"))
  }
}
