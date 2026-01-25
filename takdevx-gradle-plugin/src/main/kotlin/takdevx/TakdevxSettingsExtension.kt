package takdevx

import org.gradle.api.provider.Property

public interface TakdevxSettingsExtension {
  public val mavenOnly: Property<Boolean>
}
