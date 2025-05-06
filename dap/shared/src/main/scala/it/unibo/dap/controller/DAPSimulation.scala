package it.unibo.dap.controller

import it.unibo.dap.model.DAP.State
import it.unibo.dap.model.CTMC

/** The Distributed Asynchronous Petri Nets simulation.
  * @tparam Token the type of the tokens distributed across the network.
  */
trait DAPSimulation[Token]
    extends Simulation[CTMC, Token, State[Token]]
    with ExchangeComponent[Token]
    with NeighborhoodResolverComponent
