package it.unibo.dap

import gears.async.{ Async, AsyncOperations }

object Api:
  import it.unibo.dap.DAPSimulation.*

  def launchSimulation(
      rules: Set[DAP.Rule[String]],
      s0: DAP.State[String],
  )(port: Int, neighbors: Set[String])(using Async.Spawn, AsyncOperations): Unit =
    DAPSimulation(s0, rules)(port, neighbors).launch
