package it.unibo.dap.api

import it.unibo.dap.boundary.{InetNeighbourhoodResolver, SocketExchangeComponent}
import it.unibo.dap.controller.{Distributable, Simulation}
import it.unibo.dap.modelling.DAP.{*, given}
import it.unibo.dap.modelling.{CTMC, DAP}
import it.unibo.dap.modelling.CTMC.given_Simulatable_CTMC
import it.unibo.dap.utils.MSet

object DAPSimulation:

  def apply(s0: DAP.State[String], rules: Set[DAP.Rule[String]])(
      port: Int,
      neighbors: Set[String],
  ): Simulation[CTMC, String, State[String]] = ???// DAPSimulationImpl(s0, rules)(port, neighbors)

  import it.unibo.dap.controller.SerializableInstances.given 

  given Distributable[DAP.State[String], String] =
    new Distributable[DAP.State[String], String]:
      extension (s: DAP.State[String])
        override def msg: Option[String] = s.msg
        override def updated(msg: String): DAP.State[String] = s.copy(tokens = s.tokens union MSet(msg))

  class DAPSimulationImpl(s0: State[String], rules: Set[Rule[String]])(exchPort: Int, neighbors: Set[String])
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
