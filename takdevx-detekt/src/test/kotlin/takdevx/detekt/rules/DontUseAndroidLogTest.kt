package takdevx.detekt.rules

import assertk.assertThat
import assertk.assertions.hasSize
import dev.detekt.api.Config
import dev.detekt.test.lint
import takdevx.detekt.rules.DontUseAndroidLog.Companion.IMPORT_MESSAGE
import takdevx.detekt.rules.DontUseAndroidLog.Companion.WILDCARD_MESSAGE
import kotlin.test.Test

class DontUseAndroidLogTest {
  private val rule = DontUseAndroidLog(Config.empty)

  @Test
  fun `reports direct import of android util Log`() {
    val code = """
      import android.util.Log

      fun example() {
        Log.d("TAG", "message")
      }
    """.trimIndent()

    assertThat(rule)
      .linted(code)
      .hasOneFinding()
      .withMessage(IMPORT_MESSAGE)
  }

  @Test
  fun `reports wildcard import of android util package`() {
    val code = """
      import android.util.*

      fun example() {
        Log.d("TAG", "message")
      }
    """.trimIndent()

    assertThat(rule)
      .linted(code)
      .hasOneFinding()
      .withMessage(WILDCARD_MESSAGE)
  }

  @Test
  fun `does not report atak log import`() {
    val code = """
      import com.atakmap.coremap.log.Log

      fun example() {
        Log.d("TAG", "message")
      }
    """.trimIndent()

    assertThat(rule)
      .linted(code)
      .hasNoFindings()
  }

  @Test
  fun `does not report other android util imports`() {
    val code = """
      import android.util.ArrayMap
      import android.util.Base64

      fun example() {
        val map = ArrayMap<String, String>()
      }
    """.trimIndent()

    assertThat(rule)
      .linted(code)
      .hasNoFindings()
  }

  @Test
  fun `does not report android log imports from other packages`() {
    val code = """
      import com.example.android.util.Log

      fun example() {
        Log.d("TAG", "message")
      }
    """.trimIndent()

    assertThat(rule)
      .linted(code)
      .hasNoFindings()
  }

  @Test
  fun `reports import with alias`() {
    val code = """
      import android.util.Log as AndroidLog

      fun example() {
        AndroidLog.d("TAG", "message")
      }
    """.trimIndent()

    assertThat(rule)
      .linted(code)
      .hasOneFinding()
      .withMessage(IMPORT_MESSAGE)
  }

  @Test
  fun `reports multiple violations`() {
    val code = """
      import android.util.Log
      import android.util.*

      fun example() {
        Log.d("TAG", "message")
      }
    """.trimIndent()

    val findings = rule.lint(code)

    assertThat(findings).hasSize(2)

    assertThat(rule)
      .linted(code)
      .hasTwoFindings()
      .onFirstFinding { withMessage(IMPORT_MESSAGE) }
      .onSecondFinding { withMessage(WILDCARD_MESSAGE) }
  }

  @Test
  fun `does not report when no imports present`() {
    val code = """
      fun example() {
        println("No imports here")
      }
    """.trimIndent()

    assertThat(rule)
      .linted(code)
      .hasNoFindings()
  }
}
