package it.unibo.dap.api

import scala.scalajs.js.annotation.JSExport

/** The library entry-point language- and platform-agnostic API. */
trait Api:

  /** The [[Interface]] instance. */
  val interface: Interface

  /** The API Abstract Data Types. */
  trait ADTs:

    @JSExport
    type Neighbour = String

    @JSExport
    case class MSet[T](elems: T*)

    @JSExport
    case class Rule[Token](pre: MSet[Token], rate: Double, eff: MSet[Token], msg: Option[Token])

    @JSExport
    case class State[Token](tokens: MSet[Token], msg: Option[Token])

  /** The API interface with which platform-specific code interacts. It needs to be mixed-in with the [[ADTs]]. */
  trait Interface:
    ctx: ADTs =>

    export it.unibo.dap.controller.Serializable
    export it.unibo.dap.model.Equatable

    def simulate[Token: {Serializable, Equatable}](
        rules: Set[Rule[Token]],
        initial: State[Token],
        updateFn: State[Token] => Unit,
    )(port: Int, neighbours: Set[Neighbour]): Unit
end Api
