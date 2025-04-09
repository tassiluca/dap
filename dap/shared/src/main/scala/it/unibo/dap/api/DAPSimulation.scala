package it.unibo.dap.api

import it.unibo.dap.boundary.sockets.{ InetNeighbourhoodResolver, SocketExchangeComponent }
import it.unibo.dap.controller.{ DAPSimulation, Serializable, Simulation }
import it.unibo.dap.modelling.DAP.{ *, given }
import it.unibo.dap.modelling.{ CTMC, DAP, Equatable }
import it.unibo.dap.modelling.CTMC.given_Simulatable_CTMC

object DAPSimulation:

  def withSocket[Token: {Equatable, Serializable}](
      s0: DAP.State[Token],
      rules: Set[DAP.Rule[Token]],
  )(port: Int, neighbors: Set[String]): DAPSimulation[Token] = SocketDAPSim(s0, rules)(port, neighbors)

  import it.unibo.dap.controller.DistributableInstances.given

  private class SocketDAPSim[Token: {Equatable, Serializable}](
      s0: State[Token],
      rules: Set[Rule[Token]],
  )(exchPort: Int, neighbors: Set[String])
      extends DAPSimulation
      with SocketExchangeComponent
      with InetNeighbourhoodResolver:

    override def initial: State[Token] = s0
    override def behavior: CTMC[State[Token]] = DAP(rules)
    override def port: Port = exchPort

    override val neighbourhoodResolver: NeighbourhoodResolver = NeighbourhoodResolver.static:
      neighbors.map:
        case s"$address:$port" => (address, port.toInt)
        case _ => throw new IllegalArgumentException("Invalid address:port format")
end DAPSimulation
