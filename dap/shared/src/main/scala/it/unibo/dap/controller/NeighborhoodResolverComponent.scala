package it.unibo.dap.controller

/** A component providing the [[NeighborhoodResolver]] strategy with which, in any moment,
  * it is possible to retrieve the current set of neighbors.
  */
trait NeighborhoodResolverComponent:

  /** The type of the neighbor. */
  type Neighbor

  /** The neighborhood resolver instance. */
  val neighborhoodResolver: NeighborhoodResolver

  /** A neighborhood resolver, namely a function returning the current set of neighbors at any given time. */
  trait NeighborhoodResolver extends (() => Set[Neighbor])

  object NeighborhoodResolver:

    /** Creates a new static [[NeighborhoodResolver]] always returning the
      * same set of neighbors provided in input.
      */
    def static(net: Set[Neighbor]): NeighborhoodResolver = () => net
