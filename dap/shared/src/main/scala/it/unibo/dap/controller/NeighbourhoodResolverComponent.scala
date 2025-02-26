package it.unibo.dap.controller

trait NeighbourhoodResolverComponent:
  type Neighbour

  val neighbourhoodResolver: NeighbourhoodResolver

  trait NeighbourhoodResolver:
    def apply(): Set[Neighbour]

  object NeighbourhoodResolver:
    def static(net: Set[Neighbour]): NeighbourhoodResolver = () => net
