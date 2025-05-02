package it.unibo.dap.api

/** The library entry-point language- and platform-agnostic API. */
trait Api:

  /** The [[Interface]] instance. */
  val interface: Interface

  /** The API Abstract Data Types. */
  trait ADTs:

    import it.unibo.dap.utils.Iso
    export scala.scalajs.js.annotation.{ JSExport, JSExportAll }

    type IString
    given Iso[IString, String] = compiletime.deferred

    type IOption[T]
    given [T] => Iso[IOption[T], Option[T]] = compiletime.deferred

    type ISeq[T]
    given iseqc[T]: Conversion[ISeq[T], Seq[T]]
    given iseqcc[T]: Conversion[Seq[T], ISeq[T]]

    type IFunction1[T1, R]
    given f1c[T1, R]: Conversion[IFunction1[T1, R], T1 => R]

    type IFunction2[T1, T2, R]
    given f2c[T1, T2, R]: Conversion[IFunction2[T1, T2, R], (T1, T2) => R]

    @JSExport
    type Neighbor = IString

    @JSExport
    case class Neighborhood(neighbors: Neighbor*)

    @JSExport @JSExportAll
    case class MSet[T](elems: ISeq[T])

    @JSExport @JSExportAll
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
        serializer: IFunction1[Token, IString],
        deserializer: IFunction1[IString, Token],
        equalizer: IFunction2[Token, Token, Boolean],
    ): DASPSimulation[Token]

    @JSExport
    def launch[Token](simulation: DASPSimulation[Token], port: Int, updateFn: IFunction1[State[Token], Unit]): Unit

    @JSExport
    def stop[Token](simulation: DASPSimulation[Token]): Unit
  end Interface
end Api
