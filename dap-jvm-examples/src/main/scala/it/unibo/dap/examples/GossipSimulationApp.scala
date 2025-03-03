package it.unibo.dap.examples

import it.unibo.dap.api.ProductAPI
import it.unibo.dap.api.ProductAPI.ADTs.*

object GossipSimulationApp:

  private val gossipRules = Set[Rule](
    Rule(MSet("a", "a"), _ => 1_000, MSet("a"), None), // a|a --1000--> a
    Rule(MSet("a"), _ => 1, MSet("a"), Some("a")), // a --1--> a|^a
    Rule(MSet("a", "b"), _ => 2, MSet("a", "b"), Some("b")), // a|b --2--> a|b|^b
    Rule(MSet("b", "b"), _ => 1_000, MSet("b"), None), // b|b --1000--> b
  )

  def main(args: Array[String]): Unit =
    val port = args(0).toIntOption.getOrElse(throw IllegalArgumentException(s"Port is required: $help"))
    val net = args.drop(1).toSet
    val initial = port match
      case 2550 => State(MSet("a"), None)
      case 2553 => State(MSet("b"), None)
      case _ => State(MSet(), None)
    ProductAPI.interface.simulate(gossipRules, initial, onStateChange)(port, net)

  private def help: String = "Usage: GossipSimulationApp <port> <neighbour> [<neighbour> ...]"

  private def onStateChange(s: State): Unit =
    import java.time.LocalDateTime
    import java.time.format.DateTimeFormatter
    val output =
      s"""
         |[☕️][⏰] ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))}
         |[☕][📦] State Tokens: { ${s.tokens} }
         |[☕][💬] Message: "${s.msg}"
         |----------------------------------------
         |""".stripMargin
    println(output)

end GossipSimulationApp
