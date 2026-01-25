package takdevx.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.singleFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtImportDirective

/**
 * Blocks usage of java.io and java.nio classes.
 *
 * ATAK plugins should use com.atakmap.coremap.io.IOProviderFactory instead of direct file I/O.
 * The rule reports:
 * - Imports of java.io or java.nio packages (except java.io.File)
 * - Function calls that return java.io types (except java.io.File)
 *
 * Allowed:
 * - java.io.File constructor calls
 * - Static methods in IOProviderFactory that return java.io types
 */
internal class DontUseJavaIO(config: Config) :
  Rule(
    config = config,
    description = "Use com.atakmap.coremap.io.IOProviderFactory in place of any java.io classes",
  ),
  RequiresAnalysisApi {
  override fun visitImportDirective(importDirective: KtImportDirective) {
    super.visitImportDirective(importDirective)

    val importPath = importDirective.importedFqName ?: return
    val isStarImport = importDirective.isAllUnder
    when {
      importPath == JAVA_IO_FILE -> return
      importPath == JAVA_IO && isStarImport -> report(importDirective, WILDCARD_IO_MESSAGE)
      importPath == JAVA_NIO && isStarImport -> report(importDirective, WILDCARD_NIO_MESSAGE)
      importPath.startsWith(JAVA_IO) -> report(importDirective, IMPORT_IO_MESSAGE)
      importPath.startsWith(JAVA_NIO) -> report(importDirective, IMPORT_NIO_MESSAGE)
    }
  }

  override fun visitCallExpression(expression: KtCallExpression) {
    super.visitCallExpression(expression)

    analyze(expression) {
      val callInfo = expression.resolveToCall()?.singleFunctionCallOrNull() ?: return@analyze
      val functionSymbol = callInfo.partiallyAppliedSymbol.symbol

      // Skip constructor calls
      if (functionSymbol is KaConstructorSymbol) {
        return@analyze
      }

      // Skip calls to static functions in IOProviderFactory
      val containingSymbol = functionSymbol.containingSymbol
      if (containingSymbol is KaClassLikeSymbol) {
        val containingClass = containingSymbol.classId?.asSingleFqName()
        if (containingClass == ATAK_IO_PROVIDER) {
          return@analyze
        }
      }

      val returnType = functionSymbol.returnType
      val classSymbol = returnType.expandedSymbol ?: return@analyze
      val classId = classSymbol.classId ?: return@analyze
      val fqName = classId.asSingleFqName()

      if (fqName.startsWith(JAVA_IO) && fqName != JAVA_IO_FILE) {
        report(expression, "Call returns $fqName, use $ATAK_IO_PROVIDER instead")
      }
    }
  }

  private fun report(element: KtElement, message: String) = report(Finding(Entity.from(element), message))

  internal companion object {
    private val JAVA_IO = FqName("java.io")
    private val JAVA_NIO = FqName("java.nio")
    private val JAVA_IO_FILE = JAVA_IO.child(Name.identifier("File"))
    private val ATAK_IO_PROVIDER = FqName("com.atakmap.coremap.io.IOProviderFactory")

    internal val IMPORT_IO_MESSAGE = "Do not import java.io classes. Use $ATAK_IO_PROVIDER instead."
    internal val IMPORT_NIO_MESSAGE = "Do not import java.nio classes. Use $ATAK_IO_PROVIDER instead."
    internal val WILDCARD_IO_MESSAGE = "Wildcard import of java.io.* is not allowed. Use $ATAK_IO_PROVIDER instead."
    internal val WILDCARD_NIO_MESSAGE = "Wildcard import of java.nio.* is not allowed. Use $ATAK_IO_PROVIDER instead."
  }
}
