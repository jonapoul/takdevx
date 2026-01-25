package takdevx

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

public interface TakdevxProjectExtension {
  public val devkitVersion: Property<String>
  public val verbose: Property<Boolean>
  public val pluginId: Property<String>
  public val snapshot: Property<Boolean>
  public val requireMavenLocal: Property<Boolean>
  public val sdkPath: DirectoryProperty
  public val production: Property<Boolean>
  public val noApp: Property<Boolean>
  public val conTestEnable: Property<Boolean>
  public val staticVersion: Property<Int>
  public val conTestVersion: Property<String>
  public val conTestPath: DirectoryProperty
  public val metadataPluginId: Property<String>
}
