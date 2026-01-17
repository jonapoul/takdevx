package takdevx.dependencyguard.test

import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import takdevx.dependencyguard.test.PLUGIN_ID
import java.io.File

open class ScenarioTest {
  protected open val rootBuildFile: String = """
    plugins {
      kotlin("jvm") apply false
      id("${PLUGIN_ID}") apply false
    }
  """.trimIndent()

  protected open val subprojectBuildFiles: Map<String, String> = emptyMap()
  protected open val isGroovy: Boolean = false
  protected open val gradlePropertiesFile: String = ""
  protected open val otherFiles: Map<String, String> = emptyMap()

  @TempDir lateinit var projectRoot: File

  private val createdFiles = mutableListOf<File>()

  @AfterEach
  fun afterScenarioTest() {
    createdFiles.forEach(::deleteUntilNotEmpty)
    createdFiles.clear()
  }

  private fun deleteUntilNotEmpty(file: File) {
    file.delete()
    file.parentFile?.let { parent ->
      if (parent.exists() && parent.listFiles()?.isEmpty() == true) {
        deleteUntilNotEmpty(parent)
      }
    }
  }

  protected fun File.resolveAndMkdirs(path: String): File = resolve(path).also { it.parentFile?.mkdirs() }

  protected fun File.generateBaseline(): BuildResult = runTask("dependencyGuardBaseline").build()

  protected fun deleteCachedRestrictions() {
    System
      .getProperty("user.home")
      .let(::File)
      .resolve(".gradle")
      .resolve("caches")
      .resolve("takdevx")
      .resolve("tak-dependency-guard")
      .deleteRecursively()
  }

  protected fun <T> runScenario(
    vararg properties: Pair<String, String>,
    test: File.() -> T,
  ) {
    val settingsFile = """
      ${if (isGroovy) REPOSITORIES_GRADLE_GROOVY else REPOSITORIES_GRADLE_KTS}
      ${includeStatements()}
    """.trimIndent()

    with(projectRoot) {
      resolve(settingsFileName).writeText(settingsFile)
      resolve(buildFileName).writeText(rootBuildFile)
      with(resolve("gradle.properties")) {
        writeText(gradlePropertiesFile)
        appendText("\n")
        appendText(buildString { properties.forEach { (k, v) -> appendLine("$k=$v") } })
      }

      otherFiles.forEach { (path, contents) ->
        val file = resolve(path)
        file.parentFile?.mkdirs()
        createdFiles += file
        file.writeText(contents)
      }

      subprojectBuildFiles.forEach { (path, contents) ->
        resolve(projectPathToFilePath(path))
          .also { it.mkdirs() }
          .resolve(buildFileName)
          .writeText(contents)
      }

      test()
    }
  }

  private fun projectPathToFilePath(projectPath: String): String = projectPath
    .split(":")
    .filter { it.isNotEmpty() }
    .joinToString(separator = File.separator)

  private val buildFileName get() = if (isGroovy) "build.gradle" else "build.gradle.kts"
  private val settingsFileName get() = if (isGroovy) "settings.gradle" else "settings.gradle.kts"

  private fun includeStatements() = subprojectBuildFiles
    .keys
    .joinToString(separator = "\n") { name -> if (isGroovy) "include(':$name')" else "include(\":$name\")" }

  private companion object {
    private const val REPOSITORIES_GRADLE_KTS = """
      pluginManagement {
        repositories {
          mavenCentral()
          google()
          gradlePluginPortal()
        }
      }

      dependencyResolutionManagement {
        repositories {
          google()
          mavenCentral()
        }
      }
    """

    private const val REPOSITORIES_GRADLE_GROOVY = """
      pluginManagement {
        repositories {
          mavenCentral()
          google()
          gradlePluginPortal()
        }
      }

      dependencyResolutionManagement {
        repositories {
          google()
          mavenCentral()
        }
      }
    """
  }
}
