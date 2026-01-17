@file:Suppress("UnstableApiUsage")

import dev.detekt.gradle.Detekt
import org.gradle.api.attributes.plugin.GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
  `java-gradle-plugin`
  alias(libs.plugins.buildConfig)
  alias(libs.plugins.detekt)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kotlin)
  alias(libs.plugins.kotlinAbi)
  alias(libs.plugins.publish)
  alias(libs.plugins.publishReport)
  alias(libs.plugins.dependencyGuard)
}

dependencyGuard {
  configuration("compileClasspath")
  configuration("runtimeClasspath")
}

val javaVersion = providers.gradleProperty("takdevx.javaVersion")

kotlin {
  compilerOptions {
    explicitApi()
    allWarningsAsErrors = true
    jvmTarget = javaVersion.map(JvmTarget::fromTarget)
    freeCompilerArgs.addAll(
      "-opt-in=kotlin.RequiresOptIn",
      "-Xmulti-dollar-interpolation",
    )
  }
}

java {
  val version = javaVersion.map(JavaVersion::toVersion).get()
  sourceCompatibility = version
  targetCompatibility = version
}

detekt {
  config.from(rootProject.isolated.projectDirectory.file("config/detekt.yml"))
  buildUponDefaultConfig = true
}

val detektTasks = tasks.withType<Detekt>()
val detektCheck by tasks.registering { dependsOn(detektTasks) }
tasks.named("check").configure { dependsOn(detektCheck) }

detektTasks.configureEach {
  reports {
    html { required = true }
    sarif { required = true }
  }
  exclude { it.path.contains("generated") }
}

val pluginId = "dev.jonpoulton.takdevx.dependency-guard"

gradlePlugin {
  vcsUrl = "https://github.com/jonapoul/takdevx.git"
  website = "https://github.com/jonapoul/takdevx"
  plugins {
    create("tak") {
      id = pluginId
      implementationClass = "takdevx.dependencyguard.TakDependencyGuardPlugin"
      displayName = "TAK Dependency Guard"
      description = properties["POM_DESCRIPTION"]?.toString()
      tags.addAll("gradle", "atak", "tak", "guard", "dependency", "baseline", "check")
    }
  }
}

val testPluginClasspath by configurations.registering { isCanBeResolved = true }

dependencies {
  fun pluginDependency(plugin: Provider<PluginDependency>) =
    with(plugin.get()) { "$pluginId:$pluginId.gradle.plugin:$version" }

  api(pluginDependency(libs.plugins.dependencyGuard))
  compileOnly(pluginDependency(libs.plugins.kotlin))

  testImplementation(kotlin("stdlib"))
  testImplementation(kotlin("test"))
  testImplementation(libs.assertk)
  testImplementation(libs.junit.api)
  testImplementation(libs.junit.params)
  testPluginClasspath(pluginDependency(libs.plugins.agp))
  testPluginClasspath(pluginDependency(libs.plugins.kotlin))
  testPluginClasspath(pluginDependency(libs.plugins.dependencyGuard))
  testRuntimeOnly(libs.junit.launcher)
}

tasks.validatePlugins {
  enableStricterValidation = true
  failOnWarning = true
}

tasks.pluginUnderTestMetadata {
  pluginClasspath.from(testPluginClasspath)
}

configurations.named("apiElements").configure {
  attributes {
    attribute(
      GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
      objects.named<GradlePluginApiVersion>(providers.gradleProperty("takdevx.minimumGradleVersion").get()),
    )
  }
}

buildConfig {
  generateAtSync = true
  sourceSets.getByName("test") {
    packageName = "takdevx.dependencyguard.test"
    useKotlinOutput { topLevelConstants = true }
    buildConfigField("PLUGIN_ID", pluginId)
    buildConfigField("GRADLE_VERSION", GradleVersion.current().version)
    buildConfigField<File?>("ANDROID_HOME", androidHome())
  }
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
    exceptionFormat = FULL
    showCauses = true
    showExceptions = true
    showStackTraces = true
    showStandardStreams = true
    displayGranularity = 2
  }
}

fun androidHome(): File? {
  val androidHome = System.getenv("ANDROID_HOME")?.let(::File)
  if (androidHome?.exists() == true) {
    logger.info("Using system environment variable $androidHome as ANDROID_HOME")
    return androidHome
  }

  val localProps = rootProject.file("local.properties")
  if (localProps.exists()) {
    val properties = Properties()
    localProps.inputStream().use { properties.load(it) }
    val sdkHome = properties.getProperty("sdk.dir")?.let(::File)
    if (sdkHome?.exists() == true) {
      logger.info("Using local.properties sdk.dir $sdkHome as ANDROID_HOME")
      return sdkHome
    }
  }

  logger.warn("No Android SDK found - Android unit tests will be skipped")
  return null
}
