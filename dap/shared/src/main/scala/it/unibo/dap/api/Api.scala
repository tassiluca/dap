package it.unibo.dap.api

import gears.async.{Async, AsyncOperations}
import it.unibo.dap.controller.Simulation

object DAPApi:
  import it.unibo.dap.modelling.DAP
  import it.unibo.dap.boundary.SocketExchange
  import it.unibo.dap.controller.InetNeighbourhoodSensor

  def launchSimulation(rules: Set[DAP.Rule[String]], s0: DAP.State[String])(port: Int, neighbors: Set[String])(
    using Async.Spawn, AsyncOperations
  ): Unit =
    import it.unibo.dap.controller.DistributableInstances.given
    import it.unibo.dap.modelling.CTMC
    val simulation = new Simulation[String] with SocketExchange(port) with InetNeighbourhoodSensor:
      override def apply(): Set[(Address, Port)] = neighbors.map:
        case s"$address:$port" => (address, port.toInt)
        case _ => throw new IllegalArgumentException("Invalid address:port format")
    simulation.launch(s0, DAP.toCTMC(DAP(rules)))
