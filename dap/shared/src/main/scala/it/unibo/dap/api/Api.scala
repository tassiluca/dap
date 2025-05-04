package it.unibo.dap.api

import scala.scalajs.js.annotation.{ JSExport, JSExportAll }

/** The DAP library entry-point language- and platform-agnostic API. */
trait Api extends PlatformIndependentAPI:

  /** The [[Interface]] instance. */
  val interface: Interface

  /** The API Abstract Data Types. */
  trait ADTs extends PlatformIndependentTypes:

    @JSExport @JSExportAll
    case class Neighbor(address: IString, port: Int)

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
