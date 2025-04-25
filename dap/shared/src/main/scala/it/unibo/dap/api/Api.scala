package it.unibo.dap.api

/** The library entry-point language- and platform-agnostic API. */
trait Api:

  /** The [[Interface]] instance. */
  val interface: Interface

  /** The API Abstract Data Types. */
  trait ADTs:

    import it.unibo.dap.utils.Iso
    export scala.scalajs.js.annotation.{ JSExport, JSExportAll }

    type IOption[T]
    given [T] => Iso[IOption[T], Option[T]] = compiletime.deferred

    type ISeq[T]
    given [T] => Iso[ISeq[T], Seq[T]] = compiletime.deferred

    type IFunction1[T1, R]
    given [T1, R] => Conversion[IFunction1[T1, R], T1 => R] = compiletime.deferred

    type IFunction2[T1, T2, R]
    given [T1, T2, R] => Conversion[IFunction2[T1, T2, R], (T1, T2) => R] = compiletime.deferred

    @JSExport
    type Neighbor = String

    @JSExport
    case class Neighborhood(neighbors: Neighbor*)

    @JSExport
    case class MSet[T](elems: T*)

    @JSExport
    case class Rule[Token](pre: MSet[Token], rate: Double, eff: MSet[Token], msg: IOption[Token])

    @JSExport @JSExportAll
    case class State[Token](tokens: MSet[Token], msg: IOption[Token])
  end ADTs

  /** The API interface with which platform-specific code interacts. It needs to be mixed-in with the [[ADTs]]. */
  trait Interface:
    ctx: ADTs =>

    export it.unibo.dap.controller.Serializable
    export it.unibo.dap.model.Equatable

    @JSExport
    def simulation[Token](
        rules: ISeq[Rule[Token]],
        initialState: State[Token],
        neighborhood: ISeq[Neighbor],
        serializer: IFunction1[Token, String],
        deserializer: IFunction1[String, Token],
        equalizer: IFunction2[Token, Token, Boolean],
    ): DASPSimulation[Token]

    @JSExport
    def launch[Token](simulation: DASPSimulation[Token], port: Int, updateFn: IFunction1[State[Token], Unit]): Unit

    @JSExport
    def stop[Token](simulation: DASPSimulation[Token]): Unit

    /// DEPRECATED API - STILL HERE FOR NATIVE ///

    def simulate[Token: {Serializable, Equatable}](
        rules: Set[Rule[Token]],
        initial: State[Token],
        updateFn: State[Token] => Unit,
    )(port: Int, neighbours: Set[Neighbor]): Unit
  end Interface
end Api
