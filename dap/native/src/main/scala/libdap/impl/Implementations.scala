package libdap.impl

import it.unibo.dap.CUtils.withLogging
import libdap.aliases.size_t
import libdap.aliases.Token as CToken
import libdap.structs.{
  DAPState as CDAPState,
  MSet_Neighbour as CMsetNeighbour,
  MSet_Token as CMSetToken,
  Rule as CRule,
}

import scala.language.postfixOps
import scala.scalanative.unsafe.{ fromCString, CInt, Ptr }

object Implementations extends libdap.ExportedFunctions:

  import it.unibo.dap.api.ProductAPI.*
  import ProductApiBindings.given

  override def launch_simulation(
      rules: Ptr[CRule],
      rules_size: size_t,
      s0: Ptr[CDAPState],
      port: CInt,
      neighborhood: Ptr[CMsetNeighbour],
  ): Unit = withLogging:
    val allrules = (0 until rules_size.toInt).map(i => rules(i)).map(ruleCvt).toSet
    interface.simulate(allrules, !s0, s => scribe.info(s"State: $s"))(port, !neighborhood)

end Implementations

object ProductApiBindings:

  import it.unibo.dap.api.ProductAPI.ADTs.*

  given Conversion[CMSetToken, MSet[Token]] = m =>
    MSet(
      (0 until m.size.toInt)
        .map(i => m.elements(i))
        .map(t => fromCString((!t.value).token))
        .toList*,
    )

  given Conversion[CMsetNeighbour, Set[Neighbour]] = m =>
    (0 until m.size.toInt)
      .map(i => m.elements(i))
      .map(n => fromCString(n.value))
      .toSet

  given Conversion[CToken, Option[Token]] = t => Option(t).map(t => fromCString((!t.value).token))

  given ruleCvt: Conversion[CRule, Rule] = r =>
    val rateF = (_: MSet[Token]) => r.rate(r.preconditions)
    Rule(pre = r.preconditions, rateExp = rateF, eff = r.effects, msg = r.msg)

  given Conversion[CDAPState, State] = s => State(tokens = s.tokens, msg = s.msg)
end ProductApiBindings
