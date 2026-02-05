package takdevx.internal

import org.gradle.api.Project

internal fun Project.registerTasks(config: TakdevConfig) {
  tasks.register("getTargetVersion") { t ->
    group = "metadata"
    description = "Gets this plugin's targeted ATAK version"
    val devkitVersion = config.devkitVersion
    t.inputs.property("devkitVersion", devkitVersion)
    t.doLast { logger.lifecycle(devkitVersion.get()) }
  }
}
