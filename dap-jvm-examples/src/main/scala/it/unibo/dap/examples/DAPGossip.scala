package it.unibo.dap.examples

import gears.async.*
import gears.async.default.given
import it.unibo.dap.boundary.SocketExchange
import it.unibo.dap.controller.DistributedSimulation
import it.unibo.dap.modelling.DAP.*
import it.unibo.dap.modelling.{ CTMC, DAP }
import it.unibo.dap.utils.MSet

import scala.concurrent.duration.DurationInt

object DAPGossip:

  private val gossipRules = DAP[String](
    Rule(MSet("a", "a"), _ => 1_000, MSet("a"), ""), // a|a --1000--> a
    Rule(MSet("a"), _ => 1, MSet("a"), "a"), // a --1--> a|^a
  )

  val gossipCTMC = DAP.toCTMC(gossipRules)

@main def leftUpNode(): Unit = Async.blocking:
  import it.unibo.dap.controller.DistributableInstances.given
  import DAPGossip.*
  val exchange = SocketExchange(port = 2550, net = Set(("localhost", 2551), ("localhost", 2552)))
  Future(exchange.start)
  scribe.info("Wating 5 seconds")
  AsyncOperations.sleep(5.seconds)
  DistributedSimulation(exchange).launch(State(MSet("a"), ""), gossipCTMC) // side effect

@main def rightUpNode(): Unit = Async.blocking:
  import it.unibo.dap.controller.DistributableInstances.given
  import DAPGossip.*
  val exchange = SocketExchange(port = 2551, net = Set(("localhost", 2550), ("localhost", 2553)))
  Future(exchange.start)
  scribe.info("Wating 5 seconds")
  AsyncOperations.sleep(5.seconds)
  DistributedSimulation(exchange).launch(State(MSet(), ""), gossipCTMC) // side effect

@main def leftBtmNode(): Unit = Async.blocking:
  import it.unibo.dap.controller.DistributableInstances.given
  import DAPGossip.*
  val exchange = SocketExchange(port = 2552, net = Set(("localhost", 2550), ("localhost", 2553)))
  Future(exchange.start)
  scribe.info("Wating 5 seconds")
  AsyncOperations.sleep(5.seconds)
  DistributedSimulation(exchange).launch(State(MSet(), ""), gossipCTMC) // side effect

@main def rightBtmNode(): Unit = Async.blocking:
  import it.unibo.dap.controller.DistributableInstances.given
  import DAPGossip.*
  val exchange = SocketExchange(port = 2553, net = Set(("localhost", 2551), ("localhost", 2552)))
  Future(exchange.start)
  scribe.info("Wating 5 seconds")
  AsyncOperations.sleep(5.seconds)
  DistributedSimulation(exchange).launch(State(MSet(), ""), gossipCTMC) // side effect
