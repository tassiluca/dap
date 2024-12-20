package dap.examples

import java.util.Random

import dap.modelling.{ CTMCSimulation, DAP, DAPGrid }
import dap.modelling.CTMCSimulation.*
import dap.utils.{ Grids, MSet }

object DAPGossip:

  enum Place:
    case A, B, C
  type ID = (Int, Int)

  export Place.*
  export dap.modelling.DAP.*
  export dap.modelling.DAPGrid.*
  export dap.modelling.CTMCSimulation.*

  val gossipRules = DAP[Place](
    Rule(MSet(A, A), _ => 1000, MSet(A), MSet()), // a|a --1000--> a
    Rule(MSet(A), _ => 1, MSet(A), MSet(A)), //  a  --1--> a|^a
  )
  val gossipCTMC = DAP.toCTMC[ID, Place](gossipRules)
  val net = Grids.createRectangularGrid(5, 5)
  // an `a` initial on top LEFT
  val state = State[ID, Place](MSet(Token((0, 0), A)), MSet(), net)

@main def mainDAPGossip =
  import DAPGossip.*
  gossipCTMC
    .newSimulationTrace(state, new Random)
    .take(250)
    .toList
    .foreach: step =>
      println(step._1) // print time
      println(DAPGrid.simpleGridStateToString[Place](step._2, A)) // print state, i.e., A's
