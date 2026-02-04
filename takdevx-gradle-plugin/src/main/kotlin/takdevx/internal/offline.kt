package takdevx.internal

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.provider.Provider

/**
 * Configures offline ATAK development kit dependencies and tasks.
 *
 * This uses local SDK files instead of Maven artifacts.
 */
internal fun Project.configureOffline(devKit: DevKit, config: TakdevConfig) {
  log(config, "Configuring Offline TakDev plugin build with $devKit")

  // Apply connected test file if exists
  if (config.conTestEnable.get()) {
    val testSetupFile = config.conTestPath.get().resolve("testSetup.gradle")
    if (testSetupFile.exists()) {
      apply(mapOf("from" to testSetupFile))
      log(config, "Applied connected test artifacts from local path: ${config.conTestPath.get()}")
    } else {
      logger.warn("Warning: local test files not found. Skipping connected tests.")
    }
  }

  // Determine if we need core mapping rules (ATAK >= 5.4.0)
  val usesCoreMappingRules = config.devkitVersion.map { version ->
    compareVersions(version, "5.4.0") >= 0
  }

  log(config, "Using Core Mapping Rules: ${usesCoreMappingRules.get()}")

  // Configure application variants
  pluginManager.withPlugin("com.android.application") {
    val androidComponents = extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
    androidComponents.onVariants { variant ->
      configureOfflineVariant(variant, devKit, config, usesCoreMappingRules)
    }
  }

  // Configure library variants
  pluginManager.withPlugin("com.android.library") {
    val androidComponents = extensions.getByType(LibraryAndroidComponentsExtension::class.java)
    androidComponents.onVariants { variant ->
      configureOfflineVariant(variant, devKit, config, usesCoreMappingRules)
    }
  }
}

private fun String.capitalized() = replaceFirstChar { it.uppercase() }

@Suppress("LongMethod")
private fun Project.configureOfflineVariant(
  variant: Variant,
  devKit: DevKit,
  config: TakdevConfig,
  usesCoreMappingRules: Provider<Boolean>,
) {
  val variantName = variant.name
  log(config, "Configuring offline variant: $variantName")

  // Add API jar as dependency
  val apiDep = dependencies.create(files(devKit.apiJar.absolutePath))
  dependencies.add("${variantName}CompileOnly", apiDep)
  dependencies.add("test${variantName.capitalized()}Implementation", apiDep)
  if (variant.buildType == "debug") {
    dependencies.add("${variantName}AndroidTestCompileClasspath", apiDep)
  }

  val devFlavor = variant.flavorName?.ifEmpty { "civ" } ?: "civ"
  val devType = variant.buildType ?: "debug"
  val mappingName = "proguard-$devFlavor-$devType-mapping.txt"
  val mappingFqn = devKit.mapping.absolutePath
  val coreRulesFqn = devKit.coreRules.absolutePath

  // Configure preBuild task
  tasks.named("pre${variantName.capitalized()}Build").configure { t ->
    t.doFirst {
      // Copy or create mapping file
      if (devKit.mapping.exists()) {
        copy { c ->
          c.from(mappingFqn)
          c.into(layout.buildDirectory)
          c.rename { mappingName }
        }
        log(config, "$variantName => Copied mapping file from $mappingFqn")
      } else {
        val mappingFile = layout.buildDirectory
          .file(mappingName)
          .get()
          .asFile
        mappingFile.parentFile.mkdirs()
        mappingFile.writeText("")
        logger.warn(
          "$variantName => WARNING: no mapping file could be established, " +
            "obfuscating just the plugin to work with the development core",
        )
      }

      System.setProperty("atak.proguard.mapping", mappingFqn)

      // Add core rules if needed
      if (usesCoreMappingRules.get()) {
        log(config, "$variantName => Augmenting proguard file with ATAK core rules")
        pluginManager.withPlugin("com.android.application") {
          val android = extensions.getByType(com.android.build.api.dsl.ApplicationExtension::class.java)
          android.buildTypes.named("release") { b ->
            b.proguardFiles.add(rootProject.file(coreRulesFqn))
          }
        }
      }
    }
  }

  // Configure validateSigning tasks
  val storeName = "android_keystore"
  listOf(
    "validateSigning${variantName.capitalized()}",
    "validateSigning${variantName.capitalized()}AndroidTest",
  ).forEach { taskName ->
    try {
      tasks.named(taskName).configure { t ->
        t.doFirst {
          copy { c ->
            c.from(devKit.keystore.absolutePath)
            c.into(layout.buildDirectory)
            c.rename { storeName }
          }
          log(config, "$variantName => Copied keystore to ${layout.buildDirectory.get().asFile}/$storeName")
        }
      }
    } catch (_: UnknownTaskException) {
      log(config, "Unknown Task, skipping $taskName.")
    }
  }
}

@Suppress("ReturnCount", "CyclomaticComplexMethod")
private fun compareVersions(a: String, b: String): Int {
  val validTokens = Regex("[._]")
  val aParts = a.split(validTokens)
  val bParts = b.split(validTokens)

  for (i in 0 until maxOf(aParts.size, bParts.size)) {
    if (i == aParts.size) {
      return if (bParts[i].toIntOrNull() != null) -1 else 1
    }
    if (i == bParts.size) {
      return if (aParts[i].toIntOrNull() != null) 1 else -1
    }

    val aInt = aParts[i].toIntOrNull()
    val bInt = bParts[i].toIntOrNull()

    when {
      aInt != null && bInt != null -> {
        val cmp = aInt.compareTo(bInt)
        if (cmp != 0) return cmp
      }

      aInt != null -> {
        return 1
      }

      bInt != null -> {
        return -1
      }

      else -> {
        val cmp = aParts[i].compareTo(bParts[i])
        if (cmp != 0) return cmp
      }
    }
  }

  return 0
}
