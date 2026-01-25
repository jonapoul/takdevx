package takdevx.internal

import org.gradle.api.Project
import takdevx.TakdevxProjectExtension

internal fun Project.registerTasks(extension: TakdevxProjectExtension) {
  tasks.register("getTargetVersion") { t ->
    t.group = "metadata"
    t.description = "Gets this plugin's targeted ATAK version"
    val devkitVersion = extension.devkitVersion
    t.inputs.property("devkitVersion", devkitVersion)
    t.doLast {
      t.logger.lifecycle(devkitVersion.get())
    }
  }
}
