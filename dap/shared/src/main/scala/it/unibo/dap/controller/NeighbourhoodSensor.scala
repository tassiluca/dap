package it.unibo.dap.controller

trait NeighbourhoodSensor:
  type Neighbour

  def apply(): Set[Neighbour]

trait StaticNeighbourhoodSensor extends NeighbourhoodSensor:
  val neighbours: Set[Neighbour]

  override def apply(): Set[Neighbour] = neighbours
