package it.unibo.dap.controller

import it.unibo.dap.modelling.DAP.State
import it.unibo.dap.modelling.{ CTMC, Equatable }

/** The Distributed Asynchronous Petri Nets simulation.
  * @tparam Token the type of the tokens distributed across the network.
  */
trait DAPSimulation[Token: {Equatable, Serializable}]
    extends Simulation[CTMC, Token, State[Token]]
    with ExchangeComponent[Token]
    with NeighbourhoodResolverComponent
