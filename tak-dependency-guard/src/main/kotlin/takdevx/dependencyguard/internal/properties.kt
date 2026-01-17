package takdevx.dependencyguard.internal

import org.gradle.api.Project

internal val Project.atakVersionProperty get() = providers.gradleProperty("ATAK_VERSION")
