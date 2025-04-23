package it.unibo.dap.api

/** The library entry-point language- and platform-agnostic API. */
trait Api:
  import it.unibo.dap.controller.DAPSimulation

  /** The [[Interface]] instance. */
  val interface: Interface

  /** The API Abstract Data Types. */
  trait ADTs:
    import scala.scalajs.js.annotation.JSExport

    @JSExport
    type Neighbor = String

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

    /* Desired API:
     * - simulate(rules, initial, neighbors): Simulation
     * - launch(Simulation, updateFn): Unit
     * - stop(Simulation): Unit
     */

    def simulate[Token: {Serializable, Equatable}](
        rules: Set[Rule[Token]],
        initial: State[Token],
        updateFn: State[Token] => Unit,
    )(port: Int, neighbours: Set[Neighbor]): Unit
end Api
