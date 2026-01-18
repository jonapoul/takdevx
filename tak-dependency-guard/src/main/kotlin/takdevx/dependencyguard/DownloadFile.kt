package takdevx.dependencyguard

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import takdevx.dependencyguard.internal.TAKDEVX_TASK_GROUP
import java.net.URI

/**
 * Gradle task for downloading files from URLs.
 *
 * This task is used internally to download TAK dependency restriction files from GitHub or custom URLs. Downloaded
 * files are cached in Gradle's cache directory to avoid redundant downloads between projects.
 *
 * The task is skipped when [TakDependencyGuardExtension.restrictionsFile] is set.
 *
 * Default cache location: `~/.gradle/caches/takdevx/tak-dependency-guard/`
 *
 * @see TakDependencyGuardExtension
 */
@CacheableTask
public abstract class DownloadFile : DefaultTask() {
  /**
   * The URL to download from.
   *
   * Can be a GitHub raw content URL or any custom URL pointing to a restrictions file.
   *
   * Example:
   * `https://raw.githubusercontent.com/jonapoul/takdevx/main/atak-versions/restrictions-5.6.0.txt`
   */
  @get:Input
  public abstract val url: Property<String>

  /**
   * Destination file where downloaded content will be saved.
   *
   * Parent directories are created automatically if they don't exist.
   * The file is overwritten if it already exists.
   */
  @get:OutputFile
  public abstract val destinationFile: RegularFileProperty

  init {
    group = TAKDEVX_TASK_GROUP
    description = "Downloads the dependency restriction file needed for the checkTakDependencies task"
  }

  @TaskAction
  public fun execute() {
    val url = url.get()
    val destinationFile = destinationFile.get().asFile
    destinationFile.parentFile.mkdirs()
    logger.info("Downloading from $url to ${destinationFile.absolutePath}")
    URI(url).toURL().openStream().use { input ->
      destinationFile.outputStream().use { output ->
        input.copyTo(output)
      }
    }
  }
}
