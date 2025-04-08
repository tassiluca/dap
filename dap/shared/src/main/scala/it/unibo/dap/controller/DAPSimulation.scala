package it.unibo.dap.controller

import it.unibo.dap.modelling.DAP.State
import it.unibo.dap.modelling.{ CTMC, Equatable }

trait DAPSimulation[Token: {Equatable, Serializable}]
    extends Simulation[CTMC, Token, State[Token]]
    with ExchangeComponent[Token]
    with NeighbourhoodResolverComponent
