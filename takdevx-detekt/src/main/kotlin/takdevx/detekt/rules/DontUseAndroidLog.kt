package takdevx.detekt.rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtImportDirective

internal class DontUseAndroidLog(config: Config) : Rule(
  config = config,
  description = "Detects usages of the android.util.Log class, which should be replaced with " +
    "com.atakmap.coremap.log.Log",
) {
  override fun visitImportDirective(importDirective: KtImportDirective) {
    super.visitImportDirective(importDirective)

    val importPath = importDirective.importedFqName?.asString() ?: return
    if (importPath == ANDROID_LOG) {
      report(
        Finding(Entity.from(importDirective), IMPORT_MESSAGE),
      )
    } else if (importPath == ANDROID_UTIL && importDirective.isAllUnder) {
      report(
        Finding(Entity.from(importDirective), WILDCARD_MESSAGE),
      )
    }
  }

  internal companion object {
    private const val ANDROID_LOG = "android.util.Log"
    private const val ANDROID_UTIL = "android.util"
    private const val ATAK_LOG = "com.atakmap.coremap.log.Log"

    internal const val IMPORT_MESSAGE = "Do not use $ANDROID_LOG. Use $ATAK_LOG instead."
    internal const val WILDCARD_MESSAGE = "Wildcard import of android.util.* may include $ANDROID_LOG. " +
      "Use specific imports or $ATAK_LOG instead."
  }
}
