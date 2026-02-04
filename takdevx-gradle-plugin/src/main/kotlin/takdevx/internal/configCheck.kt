package takdevx.internal

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import java.io.File

internal fun Project.registerConfigCheckTask(config: TakdevConfig) {
  val takDevConfigCheck = tasks.register("takdevConfigCheck", TakDevConfigCheck::class.java) { t ->
    t.verbose.convention(config.verbose)
    t.isAndroidApp.convention(false)
    t.minify.convention(false)
    t.storeArchive.convention(false)
    t.useLegacyPackaging.convention(false)
    @Suppress("UnstableApiUsage")
    t.proguardFiles.unsetConvention()
  }

  pluginManager.withPlugin("com.android.application") {
    val android = extensions.getByType(ApplicationExtension::class.java)
    val release = android.buildTypes.named("release")
    takDevConfigCheck.configure { t ->
      t.isAndroidApp.set(true)
      t.minify.set(release.map { it.isMinifyEnabled })
      t.storeArchive.set(android.bundle.storeArchive.enable)
      t.proguardFiles.setFrom(release.map { it.proguardFiles })
    }
  }

  pluginManager.withPlugin("com.android.base") {
    val android = extensions.getByType(CommonExtension::class.java)
    takDevConfigCheck.configure { t ->
      t.useLegacyPackaging.set(android.packaging.jniLibs.useLegacyPackaging)
    }
  }

  pluginManager.withPlugin("base") {
    tasks.named("check") { t -> t.dependsOn(takDevConfigCheck) }
  }
}

@CacheableTask
private abstract class TakDevConfigCheck : DefaultTask() {
  @get:Input
  abstract val verbose: Property<Boolean>

  @get:Input
  abstract val isAndroidApp: Property<Boolean>

  @get:Input
  abstract val minify: Property<Boolean>

  @get:Input
  abstract val storeArchive: Property<Boolean>

  @get:Input
  abstract val useLegacyPackaging: Property<Boolean>

  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  abstract val proguardFiles: ConfigurableFileCollection

  @TaskAction
  fun execute() {
    if (isAndroidApp.get() && !minify.get()) {
      error(
        "Obfuscation disabled. " +
          "Please set minifyEnabled to true on release buildType",
      )
    }

    if (isAndroidApp.get() && storeArchive.get()) {
      error(
        "android.bundle.storeArchive.enable is enabled. " +
          "Please set storeArchive to false for proper release signing.",
      )
    }

    if (!useLegacyPackaging.get()) {
      error(
        "android.packagingOptions.jniLibs.useLegacyPackaging is disabled. " +
          "Please set useLegacyPackaging for proper plugin library loading.",
      )
    }

    if (isAndroidApp.get()) {
      val proguardFiles = proguardFiles.files.filter { it.isFile }
      if (proguardFiles.isEmpty()) error("No proguard files found!")
      proguardFiles.forEach(::checkProguardFile)
    }
  }

  private fun checkProguardFile(file: File) {
    val repackagePattern = "^-repackageclasses (.*?)$".toRegex()
    file.useLines { lines ->
      for (line in lines) {
        val match = repackagePattern.find(line)
        if (match != null) {
          val packageName = match.groupValues[1]
          if (packageName == "atakplugin.PluginTemplate") {
            error("Update -repackageclasses in $file to a value unique for your plugin.")
          } else {
            log(verbose, "Repacking classes to $packageName")
          }
        }
      }
    }
  }
}
