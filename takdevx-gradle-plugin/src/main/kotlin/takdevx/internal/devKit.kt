package takdevx.internal

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.Provider
import java.io.File

internal data class DevKit(
  val apiJar: File,
  val keystore: File,
  val mapping: File,
  val coreRules: File,
)

internal fun Settings.devKit(sdkPath: Provider<File>): DevKit? = devKit(rootDir, sdkPath)

internal fun Project.devKit(sdkPath: Provider<File>): DevKit? = devKit(rootDir, sdkPath)

private fun devKit(rootDir: File, sdkPath: Provider<File>): DevKit? =
  defaultDevKit(rootDir) ?: offlineDevKit(rootDir, sdkPath)

private fun defaultDevKit(rootDir: File): DevKit? {
  val dir = rootDir.resolve("../..")
  val kit = DevKit(
    apiJar = dir.resolve("ATAK/app/build/libs/main.jar"),
    keystore = dir.resolve("android_keystore"),
    mapping = dir.resolve("ATAK/app/build/outputs/mapping/release/mapping.txt"),
    coreRules = dir.resolve("ATAK/app/proguard-release-keep.txt"),
  )

  // mapping does not have to exist for local debug builds
  return if (kit.apiJar.isFile && kit.keystore.isFile) kit else null
}

private fun offlineDevKit(rootDir: File, sdkPath: Provider<File>): DevKit? {
  val offlinePath = listOf(
    rootDir.resolve("../.."),
    sdkPath.get(),
  ).firstOrNull { f -> f.resolve("main.jar").isFile }

  return if (offlinePath == null) {
    null
  } else {
    DevKit(
      apiJar = offlinePath.resolve("main.jar"),
      keystore = offlinePath.resolve("android_keystore"),
      mapping = offlinePath.resolve("mapping.txt"),
      coreRules = offlinePath.resolve("proguard-release-keep.txt"),
    )
  }
}
