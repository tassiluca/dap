package it.unibo.dap.api

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportAll, JSExportTopLevel }

object JSProductAPI extends ProductApi:

  override val interface: ProductInterface = JSInterface

  @JSExportTopLevel("DAPApi")
  @JSExportAll
  object JSInterface extends ProductInterface with JSADTs:
    override given ExecutionContext = JSExecutionContext.queue

    def ruleOf[Token](pre: MSet[Token], rate: Double, eff: MSet[Token], msg: Token): Rule[Token] =
      Rule(pre, rate, eff, Some(msg))

    def ruleOf[Token](pre: MSet[Token], rate: Double, eff: MSet[Token]): Rule[Token] = Rule(pre, rate, eff, None)

    def stateOf[Token](tokens: MSet[Token], msg: Token): State[Token] = State(tokens, Some(msg))

    def stateOf[Token](tokens: MSet[Token]): State[Token] = State(tokens, None)

    def simulate(
        rules: js.Array[Rule[Token]],
        initial: State[Token],
        port: Int,
        neighbours: js.Array[Neighbour],
        updateFn: js.Function1[State[Token], Unit],
    ): Unit = simulate(rules.toSet, initial, updateFn)(port, neighbours.toSet)
  end JSInterface

  trait JSADTs extends ADTs:
    import it.unibo.dap.model.Equatable
    type Token = String
    export it.unibo.dap.controller.SerializableInstances.given_Serializable_String
    given Equatable[String] = _ == _
end JSProductAPI
