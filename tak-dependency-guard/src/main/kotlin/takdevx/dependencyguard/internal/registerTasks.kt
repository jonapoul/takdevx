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
  // Register the download task on the root project to ensure it's shared across all modules
  // This prevents implicit dependency issues when multiple modules use the plugin
  val rootProject = rootProject
  val taskName = "downloadTakDependencies"

  if (rootProject.tasks.findByName(taskName) != null) {
    // Task already exists on root project, return a provider for it
    return rootProject.tasks.named(taskName, DownloadFile::class.java)
  }

  // Register the task on root project for the first time
  // Use root project's property providers to ensure consistent configuration across all modules
  val filename = extension
    .atakVersion
    .orElse(rootProject.atakVersionProperty)
    .map { "restrictions-$it.txt" }

  val downloadUrl = extension
    .restrictionsUrl
    .orElse(filename.map { "$BASE_URL/$it" })

  val cacheDir = gradle.gradleUserHomeDir
    .resolve("caches")
    .resolve("takdevx")
    .resolve("tak-dependency-guard")

  return rootProject.tasks.register(taskName, DownloadFile::class.java) { t ->
    t.url.set(downloadUrl)
    t.destinationFile.set(cacheDir.resolve("restrictions.txt"))
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
    t.reportFile.set(layout.buildDirectory.file("reports/takdevx/tak-dependency-guard.txt"))
    t.allowedDependencies.set(extension.allowedDependencies)
  }
}
