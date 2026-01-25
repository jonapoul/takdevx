package takdevx.test

import assertk.Assert
import assertk.assertions.support.expected
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS

public fun Assert<BuildResult>.taskSucceeded(name: String): Assert<BuildResult> =
  taskHadResult(name, expected = SUCCESS)

public fun Assert<BuildResult>.taskFailed(name: String): Assert<BuildResult> = taskHadResult(name, expected = FAILED)

public fun Assert<BuildResult>.taskHadResult(
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

public fun Assert<BuildResult>.outputContains(expected: String): Assert<BuildResult> = transform { result ->
  val output = result.output
  if (output.contains(expected)) {
    result
  } else {
    expected("output to contain '$expected', actually got:\n$output")
  }
}

public fun Assert<BuildResult>.outputDoesNotContain(expected: String): Assert<BuildResult> = transform { result ->
  val output = result.output
  if (!output.contains(expected)) {
    result
  } else {
    expected("output to not contain '$expected', actually got:\n$output")
  }
}

public fun Assert<BuildResult>.outputContains(expected: Regex): Assert<BuildResult> = transform { result ->
  val output = result.output
  if (output.contains(expected)) {
    result
  } else {
    expected("output to contain '$expected', actually got:\n$output")
  }
}

public fun Assert<String>.contains(expected: String): Assert<String> = transform { actual ->
  if (actual.contains(expected)) {
    actual
  } else {
    expected("string to contain '$expected' - actual = $actual")
  }
}

public fun Assert<String>.doesNotContain(expected: String): Assert<String> = transform { actual ->
  if (!actual.contains(expected)) {
    actual
  } else {
    expected("string to not contain '$expected' - actual = $actual")
  }
}
