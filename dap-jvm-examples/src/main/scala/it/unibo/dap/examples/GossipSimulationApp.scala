package it.unibo.dap.examples

import gears.async.Async
import gears.async.default.given
import it.unibo.dap.Api.*
import it.unibo.dap.modelling.DAP
import it.unibo.dap.modelling.DAP.*
import it.unibo.dap.utils.MSet

object GossipSimulationApp:

  private val gossipRules = Set[Rule[String]](
    Rule(MSet("a", "a"), _ => 1_000, MSet("a"), None), // a|a --1000--> a
    Rule(MSet("a"), _ => 1, MSet("a"), Some("a")), // a --1--> a|^a
  )

  @main def leftUpNode(): Unit = Async.blocking:
    launchSimulation(gossipRules, State(MSet("a"), None))(2550, Set("localhost:2551", "localhost:2552"))

  @main def rightUpNode(): Unit = Async.blocking:
    launchSimulation(gossipRules, State(MSet(), None))(2551, Set("localhost:2550", "localhost:2553"))

  @main def leftBtmNode(): Unit = Async.blocking:
    launchSimulation(gossipRules, State(MSet(), None))(2552, Set("localhost:2550", "localhost:2553"))

  @main def rightBtmNode(): Unit = Async.blocking:
    launchSimulation(gossipRules, State(MSet(), None))(2553, Set("localhost:2551", "localhost:2552"))
