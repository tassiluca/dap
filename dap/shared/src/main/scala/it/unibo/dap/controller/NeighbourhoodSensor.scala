package it.unibo.dap.controller

trait NeighbourhoodSensor:
  type Neighbour

  def apply(): Set[Neighbour]

trait InetNeighbourhoodSensor extends NeighbourhoodSensor:
  type Address = String
  type Port = Int
  type Endpoint = (Address, Port)
  override type Neighbour = Endpoint
