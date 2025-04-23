package it.unibo.dap.api

import scala.concurrent.ExecutionContext

import it.unibo.dap.boundary.sockets.{ InetNeighborhoodResolver, SocketExchangeComponent, SocketNetworking }
import it.unibo.dap.controller.DistributableInstances.given
import it.unibo.dap.controller.{ DAPSimulation, Serializable }
import it.unibo.dap.model.DAP.*
import it.unibo.dap.model.{ CTMC, DAP, Equatable }

trait SocketBasedDAPSimulation[Token: {Equatable, Serializable}](
    initialState: State[Token],
    rules: Set[Rule[Token]],
) extends DAPSimulation[Token]
    with SocketExchangeComponent[Token]
    with SocketNetworking[Token]
    with InetNeighborhoodResolver:
  override def initial: State[Token] = initialState
  override def behavior: CTMC[State[Token]] = DAP(rules)

object SocketBasedDAPSimulation:

  def withStaticNeighbors[Token: {Equatable, Serializable}](
      s0: DAP.State[Token],
      rules: Set[DAP.Rule[Token]],
      neighbors: Set[String],
  )(using ExecutionContext): SocketBasedDAPSimulation[Token] = new SocketBasedDAPSimulation[Token](s0, rules):
    override val neighborhoodResolver: NeighborhoodResolver = NeighborhoodResolver.static:
      neighbors.map:
        case s"$address:$port" => (address, port.toInt)
        case _ => throw new IllegalArgumentException("Invalid address:port format")
