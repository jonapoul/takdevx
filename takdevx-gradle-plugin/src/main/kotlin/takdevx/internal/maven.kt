@file:Suppress("TooManyFunctions")

package takdevx.internal

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolveException
import org.gradle.api.provider.Provider
import java.util.Properties

/**
 * Configures Maven-based ATAK development kit dependencies and tasks.
 *
 * This replicates the logic from the original TakDevPlugin's configureMaven method.
 */
internal fun Project.configureMaven(config: TakdevConfig) {
  log(config, "Configuring Maven TAK plugin build")

  populateKeystoreConfig(config)
  configureConnectedTests(config)

  // Determine if we need core mapping rules (ATAK >= 5.4.0)
  val usesCoreMappingRules = config.devkitVersion.map { version ->
    compareVersions(version, "5.4.0") >= 0
  }

  log(config, "Using Core Mapping Rules: ${usesCoreMappingRules.get()}")

  pluginManager.withPlugin("com.android.application") {
    val androidComponents = extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
    androidComponents.onVariants { variant ->
      configureVariant(variant, config, usesCoreMappingRules, isLibrary = false)
    }
  }

  pluginManager.withPlugin("com.android.library") {
    val androidComponents = extensions.getByType(LibraryAndroidComponentsExtension::class.java)
    androidComponents.onVariants { variant ->
      configureVariant(variant, config, usesCoreMappingRules, isLibrary = true)
    }
  }
}

private fun String.capitalized() = replaceFirstChar { it.uppercase() }

/**
 * Validates if a Maven coordinate can be resolved.
 */
private fun Project.canResolve(coord: Map<String, String>): Boolean {
  val detachedConfig = configurations.detachedConfiguration(
    dependencies.create(coord),
  )
  return try {
    detachedConfig.resolve().isNotEmpty()
  } catch (_: Exception) {
    false
  }
}

/**
 * Resolves Maven coordinates with automatic fallback to 'civ' flavor if needed.
 * Returns null if resolution fails for both the requested flavor and civ fallback.
 */
@Suppress("ReturnCount")
private fun Project.resolveWithFallback(
  coord: Map<String, String>,
  devFlavor: String,
  variantName: String,
  config: TakdevConfig,
): Map<String, String>? {
  // Try primary flavor first
  log(config, "$variantName => Attempting to resolve: $coord")
  if (canResolve(coord)) {
    return coord
  }

  // Try civ fallback if not already civ
  if (devFlavor != "civ") {
    val civCoord = coord.toMutableMap()
    civCoord["group"] = requireNotNull(civCoord["group"]).replace(oldValue = ".$devFlavor.", newValue = ".civ.")
    logger.warn("Failed to resolve $coord for $variantName. Trying civ fallback: $civCoord")

    if (canResolve(civCoord)) {
      log(config, "$variantName => Resolved using civ fallback")
      return civCoord
    }
  }

  // Detailed diagnostic error message
  val artifactType = coord["name"] ?: "unknown"
  logger.error(
    """
    |Failed to resolve ATAK $artifactType for variant '$variantName'
    |  Coordinates: ${coord["group"]}:${coord["name"]}:${coord["version"]}
    |  Repository: ${config.repoUrl.orNull ?: "not configured"}
    |  DevKit Version: ${config.devkitVersion.get()}
    |  Flavor: $devFlavor
    |
    |Possible causes:
    |  - Incorrect devkitVersion (${config.devkitVersion.get()})
    |  - Network connectivity issues
    |  - Invalid repository credentials
    |  - Flavor '$devFlavor' not published to repository
    |  - Artifact not available for this ATAK version
    |
    |Skipping variant configuration for '$variantName'
    """.trimMargin(),
  )
  return null
}

@Suppress("LongMethod", "ComplexMethod", "ReturnCount")
private fun Project.configureVariant(
  variant: Variant,
  config: TakdevConfig,
  usesCoreMappingRules: Provider<Boolean>,
  isLibrary: Boolean,
) {
  val variantName = variant.name
  log(config, "Configuring variant: $variantName")

  // Configuration names for this variant
  val apkZipConfigName = "${variantName}ApkZip"
  val mappingConfigName = "${variantName}Mapping"
  val keystoreConfigName = "${variantName}Keystore"
  val coreRulesConfigName = "${variantName}CoreRules"

  // Determine flavor and type with fallback
  val devFlavor = getDesiredFlavorName(variant, fallback = false)
  val devType = getDesiredBuildType(variant, config.production)

  log(config, "$variantName => flavor: $devFlavor, type: $devType")

  // Calculate Maven version range
  val mavenVersion = config.devkitVersion.zip(config.snapshot) { version, snapshot ->
    calculateMavenVersionRange(version, snapshot)
  }

  // Maven coordinate groups
  val mavenGroupCommon = "com.atakmap.app.$devFlavor.common"
  val mavenGroupTyped = "com.atakmap.app.$devFlavor.$devType"

  // API dependency - validate resolution with fallback
  val apiCoord = mavenVersion.map { version ->
    mapOf("group" to mavenGroupCommon, "name" to "api", "version" to version)
  }

  // Skip variant configuration if API cannot be resolved
  val resolvedApiCoord = resolveWithFallback(apiCoord.get(), devFlavor, variantName, config)
    ?: return

  log(config, "$variantName => Using API coordinate: $resolvedApiCoord")

  // Add API as compileOnly
  val apiDep = dependencies.create(resolvedApiCoord)
  dependencies.add("${variantName}CompileOnly", apiDep)
  dependencies.add("test${variantName.capitalized()}Implementation", apiDep)

  // Add for AndroidTest if debug variant
  if (variant.buildType == "debug") {
    dependencies.add("${variantName}AndroidTestCompileClasspath", apiDep)
  }

  // Javadoc dependency (optional) - with fallback to civ
  val javadocCoord = mavenVersion.map { version ->
    mapOf("group" to mavenGroupCommon, "name" to "javadoc", "version" to version)
  }

  val resolvedJavadocCoord = resolveWithFallback(javadocCoord.get(), devFlavor, variantName, config)
  if (resolvedJavadocCoord != null) {
    val javadocDep = dependencies.create(resolvedJavadocCoord)
    dependencies.add("${variantName}CompileOnly", javadocDep)
    dependencies.add("test${variantName.capitalized()}Implementation", javadocDep)
    if (variant.buildType == "debug") {
      dependencies.add("${variantName}AndroidTestCompileClasspath", javadocDep)
    }
    log(config, "$variantName => Added javadoc dependency: $resolvedJavadocCoord")
  } else {
    logger.warn("$variantName => Javadoc not available (optional)")
  }

  // APK zip configuration (only for applications)
  if (!isLibrary && !config.noApp.get()) {
    val apkZipConfig = configurations.create(apkZipConfigName) { c ->
      c.isCanBeConsumed = false
      c.isCanBeResolved = true
    }

    val apkCoord = mavenVersion.map { version ->
      mapOf("group" to mavenGroupTyped, "name" to "apk", "version" to version)
    }
    dependencies.add(apkZipConfigName, apkCoord.get())
    log(config, "$variantName => APK coordinate: ${apkCoord.get()}")

    // Add civ APK if not civ flavor
    if (devFlavor != "civ") {
      val civApkCoord = mavenVersion.map { version ->
        mapOf("group" to "com.atakmap.app.civ.$devType", "name" to "apk", "version" to version)
      }
      dependencies.add(apkZipConfigName, civApkCoord.get())
      log(config, "$variantName => Adding civ APK: ${civApkCoord.get()}")
    }

    // Configure assemble task to extract APK
    configureAssembleTask(variantName, apkZipConfig, config)
  }

  // Mapping configuration - with fallback to civ
  val mappingConfig = configurations.create(mappingConfigName) { c ->
    c.isCanBeConsumed = false
    c.isCanBeResolved = true
  }
  val mappingCoord = mavenVersion.map { version ->
    mapOf("group" to mavenGroupTyped, "name" to "mapping", "version" to version)
  }

  val resolvedMappingCoord = resolveWithFallback(mappingCoord.get(), devFlavor, variantName, config)
  if (resolvedMappingCoord == null) {
    // Skip variant configuration if mapping cannot be resolved
    return
  }

  dependencies.add(mappingConfigName, resolvedMappingCoord)
  log(config, "$variantName => Using mapping coordinate: $resolvedMappingCoord")

  // Core rules configuration (if needed)
  val coreRulesConfig = if (usesCoreMappingRules.get()) {
    val coreRulesConf = configurations.create(coreRulesConfigName) { c ->
      c.isCanBeConsumed = false
      c.isCanBeResolved = true
    }

    // Use civ flavor for core rules if not civ
    val coreRulesGroup = if (devFlavor != "civ") {
      "com.atakmap.app.civ.$devType"
    } else {
      mavenGroupTyped
    }

    val coreRulesCoord = mavenVersion.map { version ->
      mapOf("group" to coreRulesGroup, "name" to "coreRules", "version" to version)
    }
    dependencies.add(coreRulesConfigName, coreRulesCoord.get())
    log(config, "$variantName => CoreRules coordinate: ${coreRulesCoord.get()}")
    coreRulesConf
  } else {
    null
  }

  // Keystore configuration - with fallback to civ
  val keystoreConfig = configurations.create(keystoreConfigName) { c ->
    c.isCanBeConsumed = false
    c.isCanBeResolved = true
  }
  val keystoreCoord = mavenVersion.map { version ->
    mapOf("group" to mavenGroupTyped, "name" to "keystore", "version" to version)
  }

  // Skip variant configuration if keystore cannot be resolved
  val resolvedKeystoreCoord = resolveWithFallback(keystoreCoord.get(), devFlavor, variantName, config) ?: return

  dependencies.add(keystoreConfigName, resolvedKeystoreCoord)
  log(config, "$variantName => Using keystore coordinate: $resolvedKeystoreCoord")

  // Configure preBuild task
  configurePreBuildTask(
    variantName = variantName,
    devFlavor = devFlavor,
    devType = devType,
    mappingConfig = mappingConfig,
    coreRulesConfig = coreRulesConfig,
    usesCoreMappingRules = usesCoreMappingRules.get(),
    config = config,
  )

  // Configure validateSigning tasks
  configureValidateSigningTasks(variantName, keystoreConfig, config)
}

private fun Project.configureAssembleTask(
  variantName: String,
  apkZipConfig: Configuration,
  config: TakdevConfig,
) {
  tasks.named("assemble${variantName.capitalized()}").configure { t ->
    t.inputs.files(apkZipConfig)
    t.doLast {
      copy { c ->
        c.from(apkZipConfig)
        c.into(layout.buildDirectory.dir("intermediates/atak-zips"))
        c.eachFile { d ->
          val zipFileTree = project.zipTree(d.file)
          val apkTree = zipFileTree.matching { c.include("**/*.apk") }

          // Extract flavor from APK filename
          val apkFile = apkTree.singleFile
          val matcher = Regex("""(.+-([a-zA-Z]+))\.apk""").find(apkFile.name)

          if (matcher != null) {
            val (fullName, flavor) = matcher.destructured
            d.relativePath = d.relativePath.replaceLastName("$fullName.zip")

            // Copy APK contents to outputs
            project.copy { c2 ->
              c2.from(zipFileTree)
              c2.into(project.layout.buildDirectory.dir("outputs/atak-apks/$flavor"))
              c2.exclude("output-metadata.json")
            }
          }
        }
      }
      log(config, "$variantName => Extracted ATAK APKs")
    }
  }
}

@Suppress("LongParameterList")
private fun Project.configurePreBuildTask(
  variantName: String,
  devFlavor: String,
  devType: String,
  mappingConfig: Configuration,
  coreRulesConfig: Configuration?,
  usesCoreMappingRules: Boolean,
  config: TakdevConfig,
) {
  val mappingName = "proguard-$devFlavor-$devType-mapping.txt"
  val mappingFqn = layout.buildDirectory.file(mappingName)

  tasks.named("pre${variantName.capitalized()}Build").configure { t ->
    t.inputs.files(mappingConfig)
    if (coreRulesConfig != null) {
      t.inputs.files(coreRulesConfig)
    }

    t.doFirst {
      // Copy mapping file
      copy { c ->
        c.from(mappingConfig)
        c.into(layout.buildDirectory)
        c.rename { mappingName }
      }

      val mappingFile = mappingFqn.get().asFile
      System.setProperty("atak.proguard.mapping", mappingFile.absolutePath)
      log(config, "$variantName => Copied proguard mapping to ${mappingFile.absolutePath}")

      // Copy core rules if needed
      if (usesCoreMappingRules && coreRulesConfig != null) {
        val coreRulesName = "proguard-release-keep.txt"
        val coreRulesFqn = layout.buildDirectory
          .file(coreRulesName)
          .get()
          .asFile

        copy { c ->
          c.from(coreRulesConfig)
          c.into(layout.buildDirectory)
          c.rename { coreRulesName }
        }

        // Append additional keep rule (ATAK-19765+ATAK-19861)
        coreRulesFqn.appendText(
          "\n-keepclassmembers class * implements com.atakmap.spi.PriorityServiceProvider " +
            "{ public int getPriority(); }",
        )

        log(config, "$variantName => Augmented proguard file with ATAK core rules")
      }
    }
  }

  // Add core rules to proguard files if needed
  if (usesCoreMappingRules) {
    pluginManager.withPlugin("com.android.application") {
      val android = extensions.getByType(com.android.build.api.dsl.ApplicationExtension::class.java)
      android.buildTypes.named("release") { b ->
        val coreRulesFile = layout.buildDirectory
          .file("proguard-release-keep.txt")
          .get()
          .asFile
        b.proguardFile(coreRulesFile)
      }
    }
  }
}

private fun Project.configureValidateSigningTasks(
  variantName: String,
  keystoreConfig: Configuration,
  config: TakdevConfig,
) {
  val storeName = "android_keystore"
  val storeFqn = layout.buildDirectory.file(storeName)

  listOf(
    "validateSigning${variantName.capitalized()}",
    "validateSigning${variantName.capitalized()}AndroidTest",
  ).forEach { taskName ->
    tasks.named(taskName).configure { t ->
      t.inputs.files(keystoreConfig)
      t.doFirst {
        copy { c ->
          c.from(keystoreConfig)
          c.into(layout.buildDirectory)
          c.rename { storeName }
        }
        log(config, "$variantName => Copied keystore to ${storeFqn.get().asFile.absolutePath}")
      }
    }
  }
}

private fun Project.populateKeystoreConfig(config: TakdevConfig) {
  val localPropsFile = rootProject.file("local.properties")
  if (!localPropsFile.isFile) return

  val props = Properties()
  localPropsFile.inputStream().use { props.load(it) }

  val buildDir = layout.buildDirectory.get().asFile
  val defaults = mapOf(
    "takDebugKeyFile" to "$buildDir/android_keystore",
    "takDebugKeyFilePassword" to "tnttnt",
    "takDebugKeyAlias" to "wintec_mapping",
    "takDebugKeyPassword" to "tnttnt",
    "takReleaseKeyFile" to "$buildDir/android_keystore",
    "takReleaseKeyFilePassword" to "tnttnt",
    "takReleaseKeyAlias" to "wintec_mapping",
    "takReleaseKeyPassword" to "tnttnt",
  )

  var modified = false
  defaults.forEach { (key, value) ->
    if (!props.containsKey(key)) {
      props.setProperty(key, value)
      modified = true
    }
  }

  if (modified) {
    localPropsFile.outputStream().use { props.store(it, "") }
    log(config, "Populated keystore configuration in local.properties")
  }
}

@Suppress("ReturnCount")
private fun Project.configureConnectedTests(config: TakdevConfig) {
  if (!config.conTestEnable.get() || config.conTestVersion.get().isEmpty()) {
    return
  }

  val contestVersion = config.conTestVersion.get()

  val contestCoord = mapOf(
    "group" to "com.atakmap.gradle",
    "name" to "atak-connected-test",
    "version" to contestVersion,
  )

  val detachedConfig = configurations.detachedConfiguration(
    dependencies.create(contestCoord),
  )

  try {
    val contestFiles = detachedConfig.resolve()
    if (contestFiles.isEmpty()) {
      logger.warn(
        """
        |Skipping connected tests - no files found
        |  Coordinates: ${contestCoord["group"]}:${contestCoord["name"]}:${contestCoord["version"]}
        |  Repository: ${config.repoUrl.orNull ?: "not configured"}
        |
        |Verify the artifact exists in the repository for the specified version.
        """.trimMargin(),
      )
      return
    }

    val contestPath = config.conTestPath.get()
    copy { c ->
      c.from(zipTree(contestFiles.first()))
      c.into(contestPath)
    }

    val testSetupFile = contestPath.resolve("testSetup.gradle")
    if (testSetupFile.exists()) {
      apply(mapOf("from" to testSetupFile))
      log(config, "Resolved and applied connected test artifacts from maven coordinate $contestCoord")
    }
  } catch (e: ResolveException) {
    logger.warn(
      """
      |Skipping connected tests - resolution failed
      |  Coordinates: ${contestCoord["group"]}:${contestCoord["name"]}:${contestCoord["version"]}
      |  Repository: ${config.repoUrl.orNull ?: "not configured"}
      |  Error: ${e.message}
      |
      |Possible causes:
      |  - Incorrect contest version ($contestVersion)
      |  - Network connectivity issues
      |  - Invalid repository credentials
      |  - Artifact not published to repository
      """.trimMargin(),
    )
  }
}

@Suppress("UNUSED_PARAMETER")
private fun getDesiredFlavorName(variant: Variant, fallback: Boolean): String {
  val flavorName = variant.flavorName ?: "civ"
  // Fallback via matchingFallbacks not yet implemented
  return flavorName.ifEmpty { "civ" }
}

@Suppress("ReturnCount")
private fun getDesiredBuildType(variant: Variant, production: Provider<Boolean>): String {
  val buildType = variant.buildType ?: "debug"

  return when {
    buildType == "debug" -> "sdk"
    buildType == "release" && !production.get() -> "odk"
    else -> buildType
  }
}

private fun calculateMavenVersionRange(version: String, snapshot: Boolean): String {
  val tokens = version.split(".").map { it.toIntOrNull() ?: 0 }.toMutableList()
  val lowerBound = "${tokens.joinToString(".")}-SNAPSHOT"

  tokens[tokens.size - 1] += 1
  val upperBound = "${tokens.joinToString(".")}-SNAPSHOT"

  return if (snapshot) {
    "[$lowerBound, $upperBound)"
  } else {
    "($lowerBound, $upperBound)"
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
