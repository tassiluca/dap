package it.unibo.dap.controller

import it.unibo.dap.boundary.InetNeighbourhoodSensor

object DAPSimulation:

  def apply(port: Int, neighbors: Set[String]): Simulation[String] = DAPSimulationImpl(port, neighbors)

  import it.unibo.dap.boundary.SocketExchange

  private class DAPSimulationImpl(serverPort: Int, neighbors: Set[String])
      extends Simulation[String]
      with SocketExchange
      with InetNeighbourhoodSensor
      with StaticNeighbourhoodSensor:

    override val neighbours: Set[(Address, Port)] = neighbors.map:
      case s"$address:$port" => (address, port.toInt)
      case _ => throw new IllegalArgumentException("Invalid address:port format")
    override val port: Port = serverPort
