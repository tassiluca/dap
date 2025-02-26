package it.unibo.dap.controller

trait NeighbourhoodResolverComponent:
  type Neighbour

  val neighbourhoodResolver: NeighbourhoodResolver

  trait NeighbourhoodResolver extends (() => Set[Neighbour])

  object NeighbourhoodResolver:
    def static(net: Set[Neighbour]): NeighbourhoodResolver = () => net
