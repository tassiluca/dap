package it.unibo.dap.api

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportAll, JSExportTopLevel }
import scala.scalajs.js.typedarray.Uint8Array

object JSProductAPI extends ProductApi:

  override val interface: ProductInterface = JSInterface

  @JSExportTopLevel("DAPApi")
  @JSExportAll
  object JSInterface extends ProductInterface with ADTs:
    override given ExecutionContext = JSExecutionContext.queue

    def ruleOf[T](pre: MSet[T], rate: Double, eff: MSet[T], msg: T): Rule[T] = Rule(pre, rate, eff, Some(msg))

    def ruleOf[T](pre: MSet[T], rate: Double, eff: MSet[T]): Rule[T] = Rule(pre, rate, eff, None)

    def stateOf[T](tokens: MSet[T], msg: T): State[T] = State(tokens, Some(msg))

    def stateOf[T](tokens: MSet[T]): State[T] = State(tokens, None)

    def simulate[T](
        rules: js.Array[Rule[T]],
        initial: State[T],
        port: Int,
        neighbours: js.Array[Neighbour],
        updateFn: js.Function1[State[T], Unit],
        serde: js.Tuple2[js.Function1[T, Uint8Array], js.Function1[Uint8Array, T]],
        equalizer: js.Function1[js.Tuple2[T, T], Boolean],
    ): Unit =
      given Equatable[T] = (v1: T, v2: T) => equalizer((v1, v2))
      given Serializable[T] = new Serializable[T]:
        override def serialize(t: T): Array[Byte] = serde._1(t).view.map(_.toByte).toArray
        override def deserialize(bytes: Array[Byte]): T =
          val typedArray = Uint8Array(bytes.length)
          for i <- bytes.indices do typedArray(i) = bytes(i)
          serde._2(typedArray)
      simulate(rules.toSet, initial, updateFn)(port, neighbours.toSet)
  end JSInterface
end JSProductAPI
