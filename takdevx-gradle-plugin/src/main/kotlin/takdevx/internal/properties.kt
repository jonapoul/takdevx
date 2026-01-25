package takdevx.internal

import org.gradle.api.Project
import java.util.Properties

internal fun Project.populateKeystoreConfig() {
  val localProperties = rootDir.resolve("local.properties")
  if (localProperties.isFile) {
    val props = Properties()
    localProperties.bufferedReader().use(props::load)

    fun buildFile(path: String) = layout.buildDirectory
      .file(path)
      .get()
      .asFile.absolutePath
    with(props) {
      setIfAbsent("takDebugKeyFile", buildFile("android_keystore"))
      setIfAbsent("takDebugKeyFilePassword", "tnttnt")
      setIfAbsent("takDebugKeyAlias", "wintec_mapping")
      setIfAbsent("takDebugKeyPassword", "tnttnt")
      setIfAbsent("takReleaseKeyFile", buildFile("android_keystore"))
      setIfAbsent("takReleaseKeyFilePassword", "tnttnt")
      setIfAbsent("takReleaseKeyAlias", "wintec_mapping")
      setIfAbsent("takReleaseKeyPassword", "tnttnt")
    }

    localProperties.bufferedWriter().use { w -> props.store(w, "") }
  }
}

private fun Properties.setIfAbsent(key: String, value: String) {
  if (key !in this) setProperty(key, value)
}
