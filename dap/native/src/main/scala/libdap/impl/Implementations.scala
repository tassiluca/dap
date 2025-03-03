package libdap.impl

import scala.scalanative.unsafe.*

import it.unibo.dap.CUtils.withLogging
import it.unibo.dap.api.ProductAPI

import libdap.aliases.size_t
import libdap.structs.*

object Implementations extends libdap.ExportedFunctions:

  import it.unibo.dap.api.native.ProductApiBindings.{ *, given }

  override def launch_simulation(
      rules: Ptr[Rule],
      rules_size: size_t,
      s0: Ptr[DAPState],
      port: CInt,
      neighborhood: Ptr[MSet_Neighbour],
      n_state_change: CFuncPtr1[Ptr[DAPState], Unit],
  ): Unit = withLogging:
    val allRules = (0 until rules_size.toInt).map(i => rules(i)).map(ruleCvt).toSet
    val simulation = ProductAPI.interface.simulate(allRules, !s0, s => Zone(n_state_change(s.toDAPState)))
    simulation(port, !neighborhood)

end Implementations
