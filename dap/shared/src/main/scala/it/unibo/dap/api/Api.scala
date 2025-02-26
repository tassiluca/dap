package it.unibo.dap.api

import gears.async.{Async, AsyncOperations}
import it.unibo.dap.controller.{DAPSimulation, Simulation}

object Api:
  import it.unibo.dap.modelling.DAP

  def launchSimulation(rules: Set[DAP.Rule[String]], s0: DAP.State[String])(port: Int, neighbors: Set[String])(
    using Async.Spawn, AsyncOperations
  ): Unit =
    import it.unibo.dap.controller.DistributableInstances.given
    import it.unibo.dap.modelling.CTMC
    DAPSimulation(port, neighbors).launch(s0, DAP.toCTMC(DAP(rules)))
