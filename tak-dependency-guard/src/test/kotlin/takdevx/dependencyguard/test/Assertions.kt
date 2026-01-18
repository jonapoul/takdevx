package takdevx.dependencyguard.test

import assertk.Assert
import assertk.assertions.support.expected
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS

fun Assert<BuildResult>.taskSucceeded(name: String): Assert<BuildResult> = taskHadResult(name, expected = SUCCESS)

fun Assert<BuildResult>.taskFailed(name: String): Assert<BuildResult> = taskHadResult(name, expected = FAILED)

fun Assert<BuildResult>.taskHadResult(
  name: String,
  expected: TaskOutcome?,
): Assert<BuildResult> = transform { result ->
  val task = result.task(name)
  if (task?.outcome == expected) {
    result
  } else {
    expected("task result $expected for $name, actual: ${task?.outcome}. Output:\n${result.output}")
  }
}

fun Assert<BuildResult>.outputContains(expected: String): Assert<BuildResult> = transform { result ->
  val output = result.output
  if (output.contains(expected)) {
    result
  } else {
    expected("output to contain '$expected', actually got:\n$output")
  }
}

fun Assert<BuildResult>.outputContains(expected: Regex): Assert<BuildResult> = transform { result ->
  val output = result.output
  if (output.contains(expected)) {
    result
  } else {
    expected("output to contain '$expected', actually got:\n$output")
  }
}

fun Assert<String>.contains(expected: String): Assert<String> = transform { actual ->
  if (actual.contains(expected)) {
    actual
  } else {
    expected("string to contain '$expected' - actual = $actual")
  }
}

fun Assert<String>.doesNotContain(expected: String): Assert<String> = transform { actual ->
  if (!actual.contains(expected)) {
    actual
  } else {
    expected("string to not contain '$expected' - actual = $actual")
  }
}
