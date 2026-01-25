package takdevx.dependencyguard

import takdevx.test.ANDROID_HOME
import takdevx.test.GRADLE_VERSION
import takdevx.test.PLUGIN_ID
import takdevx.test.ScenarioTest

open class DependencyGuardScenarioTest : ScenarioTest() {
  override val pluginId = PLUGIN_ID
  override val androidHome = ANDROID_HOME
  override val gradleVersion = GRADLE_VERSION
}
