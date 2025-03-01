package it.unibo.dap.examples

import gears.async.Async
import gears.async.default.given
import .ADTs.*

object GossipSimulationApp:

  private val gossipRules = Set[Rule](
    Rule(MSet("a"), _ => 1, MSet("a"), Some("a")), // a --1--> a|^a
    Rule(MSet("a", "b"), _ => 2, MSet("a", "b"), Some("b")), // a|b --2--> a|b|^b
    Rule(MSet("a", "a"), _ => 1_000, MSet("a"), None), // a|a --1000--> a
    Rule(MSet("b", "b"), _ => 1_000, MSet("b"), None), // b|b --1000--> b
  )

  @main def leftUpNode(): Unit = Async.blocking:
    ProductAPI.interface.launchSimulation(gossipRules, State(MSet("a"), None), s => scribe.info(s"State: $s"))(
      2550,
      Set("localhost:2551", "localhost:2552"),
    )

  @main def rightUpNode(): Unit = Async.blocking:
    ProductAPI.interface.launchSimulation(gossipRules, State(MSet(), None), s => scribe.info(s"State: $s"))(
      2551,
      Set("localhost:2550", "localhost:2553"),
    )

  @main def leftBtmNode(): Unit = Async.blocking:
    ProductAPI.interface.launchSimulation(gossipRules, State(MSet(), None), s => scribe.info(s"State: $s"))(
      2552,
      Set("localhost:2550", "localhost:2553"),
    )

  @main def rightBtmNode(): Unit = Async.blocking:
    ProductAPI.interface.launchSimulation(gossipRules, State(MSet("b"), None), s => scribe.info(s"State: $s"))(
      2553,
      Set("localhost:2551", "localhost:2552"),
    )
end GossipSimulationApp
