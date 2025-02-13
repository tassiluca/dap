package it.unibo.dap.examples

import it.unibo.dap.modelling.DAP.*
import it.unibo.dap.modelling.{CTMC, DAP}
import it.unibo.dap.utils.{Grids, MSet}

import java.util.Random

object DAPGossip:

  enum Place:
    case A, B

  type ID = (Int, Int)

  export Place.*

  private val net = Grids.createRectangularGrid(5, 5)

  private val gossipRules = DAP[Place](
    Rule(MSet(A, A), _ => 1_000, MSet(A), MSet()), // a|a --1000--> a
    Rule(MSet(A), _ => 1, MSet(A), MSet(A)), // a --1--> a|^a
  )

  val gossipCTMC: CTMC[State[ID, Place]] = DAP.toCTMC[ID, Place](gossipRules)

  val state: State[(Int, Int), Place] =
    State[ID, Place](MSet(Token((0, 0), A)), MSet(), net) // an `a` initial on top LEFT

end DAPGossip

@main def mainDAPGossip =
  import DAPGossip.*
  import it.unibo.dap.modelling.DAPGrid
  gossipCTMC
    .simulate(state, new Random)
    .take(200)
    .toList
    .foreach: step =>
      println(step._1) // print time
      println(DAPGrid.simpleGridStateToString[Place](step.state, A)) // print state, i.e., A's
