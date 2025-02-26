package it.unibo.dap.boundary

import it.unibo.dap.controller.NeighbourhoodSensor

trait InetNeighbourhoodSensor extends NeighbourhoodSensor:
  type Address = String
  type Port = Int
  type Endpoint = (Address, Port)
  override type Neighbour = Endpoint
