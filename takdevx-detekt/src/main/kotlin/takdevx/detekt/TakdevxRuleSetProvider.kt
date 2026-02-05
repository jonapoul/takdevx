package takdevx.detekt

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetProvider
import takdevx.detekt.rules.DontUseAndroidLog
import takdevx.detekt.rules.DontUseJavaIO

// - Memory leaks - static references to Context/Activity/MapView
public class TakdevxRuleSetProvider : RuleSetProvider {
  override val ruleSetId: RuleSet.Id = RuleSet.Id("takdevx")

  override fun instance(): RuleSet = RuleSet(
    id = ruleSetId,
    rules = listOf(
      ::DontUseAndroidLog,
      ::DontUseJavaIO,
    ),
  )
}
