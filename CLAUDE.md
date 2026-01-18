# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TAK Dependency Guard is a Gradle plugin that validates dependencies in ATAK (Android Tactical Assault Kit) plugins. It ensures that ATAK plugin dependencies don't exceed version restrictions specified by the ATAK SDK.

The plugin wraps Dropbox's `dependency-guard` plugin and adds TAK-specific validation by comparing resolved dependencies against known ATAK version restrictions.

## Build Commands

### Standard Gradle Tasks
```bash
./gradlew build              # Build and test the plugin
./gradlew test               # Run unit tests
./gradlew detektMain         # Run static code analysis
./gradlew check              # Run all checks (tests, detekt, dependency guard)
./gradlew clean build        # Clean build from scratch
```

### Testing
```bash
./gradlew test --tests "ClassName.testName"  # Run a single test
./gradlew test --info        # Run tests with detailed logging
```

### Plugin Development
```bash
./gradlew publishToMavenLocal    # Publish plugin to local Maven repository for testing
./gradlew validatePlugins        # Validate plugin metadata and configuration
```

### Linting
```bash
./scripts/ktlintCheck.sh      # run linter
./scripts/ktlintFormat.sh     # attempt to auto-format
```

## Architecture

### Plugin Entry Point
- **TakDependencyGuardPlugin**: Main plugin class that applies `DependencyGuardPlugin` and registers tasks
- Creates a `takDependencyGuard` extension that wraps both dependency-guard configuration and TAK-specific properties
- Hooks into the `check` task to run TAK dependency validation

### Configuration (TakDependencyGuardExtension)
The `takDependencyGuard` extension provides a unified configuration block that combines:
- Standard dependency-guard configuration (e.g., `configuration("runtimeClasspath")`)
- TAK-specific restriction configuration

Example usage:
```kotlin
takDependencyGuard {
  // Configure which dependency configurations to track
  configuration("runtimeClasspath")

  // Configure TAK restrictions (choose one):
  atakVersion = "5.6.0"                              // Download from GitHub
  restrictionsUrl = "https://example.com/file.txt"   // Download from custom URL
  restrictionsFile = file("restrictions.txt")        // Use local file
}
```

The plugin supports four configuration methods for TAK restrictions (in priority order):
1. `atakVersion` property - downloads restrictions file from GitHub for that ATAK version
2. `restrictionsUrl` property - downloads from a custom URL
3. `restrictionsFile` property - uses a local restrictions file

Only ONE of the above three configuration methods should be set. The plugin validates this in `validateConfig.kt`.

The `ATAK_VERSION` Gradle property acts as an automatic fallback for the `atakVersion` extension property, in case none of these three are set.

### Task Chain
1. **downloadTakDependencies** (`DownloadFile` task)
   - Downloads restrictions file from GitHub or custom URL
   - Cached in `~/.gradle/caches/takdevx/tak-dependency-guard/restrictions.txt`
   - Skipped if `restrictionsFile` is set

2. **dependencyGuard** (from Dropbox plugin)
   - Generates dependency baseline files in `dependencies/` directory
   - Tracks resolved dependencies per configuration
   - Configured via `takDependencyGuard { configuration(...) }` which delegates to the underlying extension
   - Not explicitly called by this plugin, but this plugin does depend on its outputs

3. **checkTakDependencies** (`CheckDependencies` task)
   - Reads dependency guard baseline files
   - Compares versions against restrictions file
   - Uses Gradle's internal `VersionComparator` for semantic version comparison
   - Generates failure report in `build/reports/takdevx/tak-dependency-guard.txt`
   - Fails build if any dependencies exceed ATAK version limits

### Internal Package (`takdevx.**.internal`)
- **registerTasks.kt**: Task registration logic, dependency wiring
- **properties.kt**: Extension property helpers (e.g., `atakVersionProperty`)
- **validateConfig.kt**: Configuration validation (warns if multiple config options set)

### Restrictions File Format
The restrictions files in `atak-versions/` contain maximum allowed dependency versions:
```
group:artifact:maxVersion
androidx.core:core:1.17.0
androidx.fragment:fragment:1.8.9
```

### Version Comparison
Uses Gradle's `DefaultVersionComparator` and `VersionParser` to handle semantic versioning, pre-releases, and metadata correctly (see `CheckDependencies.kt`).

## Code Style

### Kotlin Configuration
- Explicit API mode enabled (`explicitApi()`)
- All warnings treated as errors
- Java 17 target (configurable via `takdevx.javaVersion` property)
- Detekt for static analysis (config in `config/detekt.yml`)

### Testing
- Uses JUnit 6
- Test utilities in `tak-dependency-guard/src/test/kotlin/takdevx/dependencyguard/test/`:
  - **ScenarioTest.kt**: Test scenario DSL for creating temporary Gradle projects
  - **GradleRunners.kt**: Gradle TestKit runner helpers
  - **Assertions.kt**: Custom assertions for Gradle build results
- Tests run with configuration cache enabled
- Each test case in a separate implementation of `ScenarioTest`

## Important Notes

- The `TakDependencyGuardExtension` wraps the `DependencyGuardPluginExtension` and delegates `configuration()` calls to it, providing a unified configuration API
- The extension uses lazy delegation to avoid configuration cache serialization issues with the wrapped extension
- The plugin relies on accessing internal Dropbox Dependency Guard properties (`projectDirectoryDependenciesDir`) via reflection in `registerTasks.kt:34`
- Minimum Gradle version is 8.13 (set in `gradle.properties`)
- Plugin uses build configuration caching extensively
- All tasks are `@CacheableTask` for build performance
