package it.unibo.dap.api

import scala.concurrent.ExecutionContext

import it.unibo.dap.boundary.sockets.{ InetNeighborhoodResolver, SocketExchangeComponent, SocketNetworking }
import it.unibo.dap.controller.DistributableInstances.given
import it.unibo.dap.controller.{ DAPSimulation, Serializable }
import it.unibo.dap.model.DAP.*
import it.unibo.dap.model.{ CTMC, DAP, Equatable }

object DAPSimulation:

  def apply[Token: {Equatable, Serializable}](s0: DAP.State[Token], rules: Set[DAP.Rule[Token]])(
      port: Int,
      neighbors: Set[String],
  )(using ExecutionContext): DAPSimulation[Token] = DAPSimulationImpl(s0, rules)(port, neighbors)

  private class DAPSimulationImpl[Token: {Equatable, Serializable}](
      s0: State[Token],
      rules: Set[Rule[Token]],
  )(exchPort: Int, neighbors: Set[String])(using ExecutionContext)
      extends DAPSimulation
      with SocketExchangeComponent
      with InetNeighborhoodResolver
      with SocketNetworking[Token]:

    override def initial: State[Token] = s0
    override def behavior: CTMC[State[Token]] = DAP(rules)
    override def port: Port = exchPort

    override val neighborhoodResolver: NeighborhoodResolver = NeighborhoodResolver.static:
      neighbors.map:
        case s"$address:$port" => (address, port.toInt)
        case _ => throw new IllegalArgumentException("Invalid address:port format")
end DAPSimulation
