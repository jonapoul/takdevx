package takdevx.dependencyguard

import assertk.Assert
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assumptions.abort
import takdevx.test.ANDROID_HOME
import assertk.assertions.contains as defaultContains
import assertk.assertions.doesNotContain as defaultDoesNotContain

fun Assert<String>.contains(expected: String): Assert<String> = transform { actual ->
  assertThat(actual).defaultContains(expected)
  actual
}

fun Assert<String>.doesNotContain(expected: String): Assert<String> = transform { actual ->
  assertThat(actual).defaultDoesNotContain(expected)
  actual
}

fun Assert<GradleRunner>.withAndroidSdk(): Assert<GradleRunner> = transform { runner ->
  val home = ANDROID_HOME
  if (home == null) {
    val message = "No ANDROID_HOME value supplied for an Android test"
    if (System.getProperty("CI") != null) error(message) else abort(message)
  } else {
    runner.withEnvironment(mapOf("ANDROID_HOME" to home.absolutePath))
  }
}
