package it.unibo.dap.examples

import gears.async.*
import gears.async.default.given
import it.unibo.dap.core.DistributedSimulation
import it.unibo.dap.modelling.DAP.*
import it.unibo.dap.modelling.{ CTMC, DAP }
import it.unibo.dap.utils.{ Grids, MSet }
import scala.concurrent.duration.DurationInt

object DAPGossip:

  private val gossipRules = DAP(
    Rule(MSet("a", "a"), _ => 1_000, MSet("a"), ""), // a|a --1000--> a
    Rule(MSet("a"), _ => 1, MSet("a"), "a"), // a --1--> a|^a
  )

  val gossipCTMC: CTMC[State] = DAP.toCTMC(gossipRules)

@main def leftUpNode(): Unit = Async.blocking:
  import DAPGossip.*
  val simulation = DistributedSimulation.create(port = 2550, network = Set(("localhost", 2551), ("localhost", 2552)))
  scribe.info("Wating 5 seconds")
  AsyncOperations.sleep(5.seconds)
  simulation.of(State(MSet("a"), ""), gossipCTMC)

@main def rightUpNode(): Unit = Async.blocking:
  import DAPGossip.*
  val simulation = DistributedSimulation.create(port = 2551, network = Set(("localhost", 2550), ("localhost", 2553)))
  scribe.info("Wating 4 seconds")
  AsyncOperations.sleep(5.seconds)
  simulation.of(State(MSet(), ""), gossipCTMC)

@main def leftBtmNode(): Unit = Async.blocking:
  import DAPGossip.*
  val simulation = DistributedSimulation.create(port = 2552, network = Set(("localhost", 2550), ("localhost", 2553)))
  scribe.info("Wating 3 seconds")
  AsyncOperations.sleep(5.seconds)
  simulation.of(State(MSet(), ""), gossipCTMC)

@main def rightBtmNode(): Unit = Async.blocking:
  import DAPGossip.*
  val simulation = DistributedSimulation.create(port = 2553, network = Set(("localhost", 2551), ("localhost", 2552)))
  scribe.info("Wating 2 seconds")
  AsyncOperations.sleep(5.seconds)
  simulation.of(State(MSet(), ""), gossipCTMC)
