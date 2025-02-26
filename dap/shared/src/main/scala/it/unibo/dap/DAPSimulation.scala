package it.unibo.dap

import it.unibo.dap.boundary.{ InetNeighbourhoodResolver, SocketExchangeComponent }
import it.unibo.dap.controller.{ ExchangeComponent, Simulation }

object DAPSimulation:

  def apply(port: Int, neighbors: Set[String]): Simulation[String] = DAPSimulationImpl(port, neighbors)

  private class DAPSimulationImpl(serverPort: Int, neighbors: Set[String])
      extends Simulation[String]
      with SocketExchangeComponent
      with InetNeighbourhoodResolver:

    override lazy val port: Port = serverPort

    override val neighbourhoodResolver: NeighbourhoodResolver = NeighbourhoodResolver.static:
      neighbors.map:
        case s"$address:$port" => (address, port.toInt)
        case _ => throw new IllegalArgumentException("Invalid address:port format")
