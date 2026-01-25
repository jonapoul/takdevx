package takdevx.detekt.rules

import assertk.assertThat
import dev.detekt.api.Config
import dev.detekt.test.junit.KotlinCoreEnvironmentTest
import dev.detekt.test.utils.KotlinEnvironmentContainer
import takdevx.detekt.rules.DontUseJavaIO.Companion.IMPORT_IO_MESSAGE
import takdevx.detekt.rules.DontUseJavaIO.Companion.IMPORT_NIO_MESSAGE
import takdevx.detekt.rules.DontUseJavaIO.Companion.WILDCARD_IO_MESSAGE
import takdevx.detekt.rules.DontUseJavaIO.Companion.WILDCARD_NIO_MESSAGE
import kotlin.test.Test

@KotlinCoreEnvironmentTest
class DontUseJavaIOTest(private val env: KotlinEnvironmentContainer) {
  private val rule = DontUseJavaIO(Config.empty)

  @Test
  fun `reports direct import of java io FileInputStream`() {
    val code = """
      import java.io.FileInputStream

      fun example() {
        val stream = FileInputStream("file.txt")
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasOneFinding()
      .withMessage(IMPORT_IO_MESSAGE)
  }

  @Test
  fun `reports direct import of java io BufferedReader`() {
    val code = """
      import java.io.BufferedReader

      fun example() {
        val reader = BufferedReader(null)
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasOneFinding()
      .withMessage(IMPORT_IO_MESSAGE)
  }

  @Test
  fun `reports wildcard import of java io`() {
    val code = """
      import java.io.*

      fun example() {
        val stream = FileInputStream("file.txt")
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasOneFinding()
      .withMessage(WILDCARD_IO_MESSAGE)
  }

  @Test
  fun `reports direct import of java nio Files`() {
    val code = """
      import java.nio.file.Files

      fun example() {
        Files.exists(null)
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasOneFinding()
      .withMessage(IMPORT_NIO_MESSAGE)
  }

  @Test
  fun `reports wildcard import of java nio package`() {
    val code = """
      import java.nio.*

      fun example() {
        val buffer = ByteBuffer.allocate(10)
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasOneFinding()
      .withMessage(WILDCARD_NIO_MESSAGE)
  }

  @Test
  fun `reports wildcard import of java nio subpackage`() {
    val code = """
      import java.nio.file.*

      fun example() {
        Files.exists(null)
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasOneFinding()
      .withMessage(IMPORT_NIO_MESSAGE)
  }

  @Test
  fun `does not report java io File import`() {
    val code = """
      import java.io.File

      fun example() {
        val file = File("path")
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasNoFindings()
  }

  @Test
  fun `reports multiple import violations`() {
    val code = """
      import java.io.FileInputStream
      import java.io.BufferedReader
      import java.nio.file.Files

      fun example() {
        val stream = FileInputStream("file.txt")
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasNumFindings(expected = 3)
      .onFirstFinding { withMessage(IMPORT_IO_MESSAGE) }
      .onSecondFinding { withMessage(IMPORT_IO_MESSAGE) }
      .onThirdFinding { withMessage(IMPORT_NIO_MESSAGE) }
  }

  @Test
  fun `reports call that returns FileInputStream`() {
    val code = """
      import java.io.FileInputStream

      fun getStream(): FileInputStream = FileInputStream("file.txt")

      fun example() {
        val stream = getStream()
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasTwoFindings()
      .onFirstFinding { withMessage(IMPORT_IO_MESSAGE) }
      .onSecondFinding { messageContains("java.io.FileInputStream") }
  }

  @Test
  fun `reports FileInputStream constructor call`() {
    val code = """
      import java.io.FileInputStream

      fun example() {
        val stream = FileInputStream("file.txt")
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasOneFinding()
      .withMessage(IMPORT_IO_MESSAGE)
  }

  @Test
  fun `reports BufferedReader constructor call`() {
    val code = """
      import java.io.BufferedReader
      import java.io.FileReader

      fun example() {
        val reader = BufferedReader(FileReader("file.txt"))
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasNumFindings(expected = 2)
      .onFirstFinding { withMessage(IMPORT_IO_MESSAGE) }
      .onSecondFinding { withMessage(IMPORT_IO_MESSAGE) }
  }

  @Test
  fun `does not report File constructor call`() {
    val code = """
      import java.io.File

      fun example() {
        val file = File("path")
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasNoFindings()
  }

  @Test
  fun `does not report File method calls`() {
    val code = """
      import java.io.File

      fun example() {
        val file = File("path")
        val exists = file.exists()
        val length = file.length()
        val isFile = file.isFile
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasNoFindings()
  }

  @Test
  fun `does not report non-java-io calls`() {
    val code = """
      fun getString(): String = "hello"

      fun example() {
        val result = getString()
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasNoFindings()
  }

  @Test
  fun `does not report ATAK IOProvider usage`() {
    val code = """
      import com.atakmap.coremap.io.IOProviderFactory

      fun example() {
        val stream = IOProviderFactory.getInputStream()
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code, IO_PROVIDER_FACTORY)
      .hasNoFindings()
  }

  @Test
  fun `reports import with alias`() {
    val code = """
      import java.io.FileInputStream as FIS

      fun example() {
        val stream = FIS("file.txt")
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasOneFinding()
      .withMessage(IMPORT_IO_MESSAGE)
  }

  @Test
  fun `does not report when no imports present`() {
    val code = """
      fun example() {
        println("No java.io imports here")
      }
    """.trimIndent()

    assertThat(rule)
      .lintedWithContext(env, code)
      .hasNoFindings()
  }

  private companion object {
    const val IO_PROVIDER_FACTORY = """
      package com.atakmap.coremap.io

      object IOProviderFactory {
        fun getInputStream(): java.io.InputStream = TODO()
      }
    """
  }
}
