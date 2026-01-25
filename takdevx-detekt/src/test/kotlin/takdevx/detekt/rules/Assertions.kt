package takdevx.detekt.rules

import assertk.Assert
import assertk.assertions.support.expected
import assertk.assertions.support.show
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.test.lint
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.KotlinEnvironmentContainer
import org.intellij.lang.annotations.Language

fun Assert<Rule>.linted(
  @Language("kotlin") code: String,
) = transform { rule -> rule.lint(code) }

fun <T> Assert<T>.lintedWithContext(
  environment: KotlinEnvironmentContainer,
  @Language("kotlin") code: String,
  @Language("kotlin") vararg dependencyContents: String,
) where T : Rule, T : RequiresAnalysisApi = transform { rule ->
  rule.lintWithContext(environment, code, *dependencyContents)
}

fun Assert<List<Finding>>.hasNoFindings() = transform { findings ->
  if (!findings.isEmpty()) expected("to be empty but was:${show(findings)}")
  findings
}

fun Assert<List<Finding>>.hasNumFindings(expected: Int) = transform { findings ->
  if (findings.size != expected) {
    expected("to have $expected but had ${findings.size}: ${show(findings)}")
  }
  findings
}

fun Assert<List<Finding>>.hasOneFinding() = hasNumFindings(expected = 1).transform { it[0] }

fun Assert<List<Finding>>.hasTwoFindings() = hasNumFindings(expected = 2)

fun Assert<List<Finding>>.onFirstFinding(block: Assert<Finding>.() -> Unit) = onFinding(index = 0, block)

fun Assert<List<Finding>>.onSecondFinding(block: Assert<Finding>.() -> Unit) = onFinding(index = 1, block)

fun Assert<List<Finding>>.onThirdFinding(block: Assert<Finding>.() -> Unit) = onFinding(index = 2, block)

fun Assert<List<Finding>>.onFinding(index: Int, block: Assert<Finding>.() -> Unit) =
  transform { findings ->
    assertThat(findings[index]).block()
    findings
  }

fun Assert<Finding>.messageContains(expected: String) = transform { finding ->
  if (expected !in finding.message) {
    expected("to have message containing '$expected', but had '${finding.message}': ${show(finding)}")
  }
  finding
}

fun Assert<Finding>.withMessage(expected: String) = transform { finding ->
  if (finding.message != expected) {
    expected("to have message '$expected', but had '${finding.message}': ${show(finding)}")
  }
  finding
}
