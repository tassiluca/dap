package it.unibo.dap

import it.unibo.dap.boundary.{ InetNeighbourhoodResolver, SocketExchangeComponent }
import it.unibo.dap.controller.Simulation
import it.unibo.dap.modelling.DAP
import it.unibo.dap.modelling.DAP.{ *, given }
import it.unibo.dap.controller.DistributableInstances.given_Distributable_State_T
import it.unibo.dap.modelling.CTMC
import it.unibo.dap.modelling.CTMC.given_Simulatable_CTMC

object DAPSimulation:

  export modelling.DAP

  def apply(s0: DAP.State[String], rules: Set[DAP.Rule[String]])(
      port: Int,
      neighbors: Set[String],
  ): Simulation[CTMC, String, State[String]] = DAPSimulationImpl(s0, rules)(port, neighbors)

  private class DAPSimulationImpl(s0: State[String], rules: Set[Rule[String]])(exchPort: Int, neighbors: Set[String])
      extends Simulation[CTMC, String, State[String]]
      with SocketExchangeComponent
      with InetNeighbourhoodResolver:

    override def initial: State[Address] = s0
    override def behavior: CTMC[State[Address]] = DAP(rules)
    override def port: Port = exchPort

    override val neighbourhoodResolver: NeighbourhoodResolver = NeighbourhoodResolver.static:
      neighbors.map:
        case s"$address:$port" => (address, port.toInt)
        case _ => throw new IllegalArgumentException("Invalid address:port format")
end DAPSimulation
