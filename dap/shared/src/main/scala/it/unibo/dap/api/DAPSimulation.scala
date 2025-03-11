package it.unibo.dap.api

import it.unibo.dap.boundary.sockets.{InetNeighbourhoodResolver, SocketExchangeComponent}
import it.unibo.dap.controller.{ Distributable, Simulation }
import it.unibo.dap.modelling.DAP.{ *, given }
import it.unibo.dap.modelling.{ CTMC, DAP }
import it.unibo.dap.modelling.CTMC.given_Simulatable_CTMC
import it.unibo.dap.utils.MSet
import it.unibo.dap.boundary.Serializable

trait DAPSimulation[Token: Serializable]
    extends Simulation[CTMC, Token, State[Token]]
    with SocketExchangeComponent[Token]
    with InetNeighbourhoodResolver

object DAPSimulation:

  def apply[Token: Serializable](s0: DAP.State[Token], rules: Set[DAP.Rule[Token]])(
      port: Int,
      neighbors: Set[String],
  ): DAPSimulation[Token] = DAPSimulationImpl(s0, rules)(port, neighbors)

  given [T: Serializable] => Distributable[DAP.State[T], T] =
    new Distributable[DAP.State[T], T]:
      extension (s: DAP.State[T])
        override def msg: Option[T] = s.msg
        override def updated(msg: T): DAP.State[T] = s.copy(tokens = s.tokens union MSet(msg))

  private class DAPSimulationImpl[Token: Serializable](s0: State[Token], rules: Set[Rule[Token]])(
      exchPort: Int,
      neighbors: Set[String],
  ) extends DAPSimulation:

    override def initial: State[Token] = s0
    override def behavior: CTMC[State[Token]] = DAP(rules)
    override def port: Port = exchPort

    override val neighbourhoodResolver: NeighbourhoodResolver = NeighbourhoodResolver.static:
      neighbors.map:
        case s"$address:$port" => (address, port.toInt)
        case _ => throw new IllegalArgumentException("Invalid address:port format")
end DAPSimulation
