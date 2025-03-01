package it.unibo.dap.api

import gears.async.{ Async, AsyncOperations }

trait Api:

  val interface: Interface

  object ADTs:
    type Token = String
    type Neighbour = String
    case class MSet[T](elems: T*)
    case class Rule(pre: MSet[Token], rateExp: MSet[Token] => Double, eff: MSet[Token], msg: Option[Token])
    case class State(tokens: MSet[Token], msg: Option[Token])

  trait Interface:
    import ADTs.*

    def simulate(
        rules: Set[Rule],
        initial: State,
        updateFn: State => Unit,
    )(port: Int, neighbours: Set[Neighbour]): Unit
