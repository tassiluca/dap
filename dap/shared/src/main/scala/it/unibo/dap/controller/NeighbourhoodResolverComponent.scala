package it.unibo.dap.controller

/** A component providing the [[NeighbourhoodResolver]] strategy with which, in any moment,
  * it is possible to retrieve the current set of neighbours.
  */
trait NeighbourhoodResolverComponent:

  /** The type of the neighbours. */
  type Neighbour

  /** The neighbourhood resolver instance. */
  val neighbourhoodResolver: NeighbourhoodResolver

  /** A neighbourhood resolver encoded as a simple function returning the current set of neighbours. */
  trait NeighbourhoodResolver extends (() => Set[Neighbour])

  object NeighbourhoodResolver:

    /** Creates a new static [[NeighbourhoodResolver]] always returning the
      * same set of neighbours provided in input.
      */
    def static(net: Set[Neighbour]): NeighbourhoodResolver = () => net
