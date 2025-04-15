package it.unibo.dap.api

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportAll, JSExportTopLevel }

@JSExportTopLevel("ProductAPI")
object JSProductAPI extends ProductApi:

  override val interface: ProductInterface = JSInterface

  @JSExportAll
  @JSExport
  object JSInterface extends ProductInterface with JSADTs:
    override given ExecutionContext = JSExecutionContext.queue

    def launchSimulation(
        rules: js.Array[JSRule],
        initial: JSState,
        port: Int,
        neighbourhood: js.Array[Neighbour],
        onStateChange: js.Function1[JSState, Unit],
    ): Unit = simulate(rules.toSet, initial, onStateChange(_))(port, neighbourhood.toSet)

  end JSInterface

  trait JSADTs extends ADTs:
    import it.unibo.dap.model.Equatable

    type Token = String

    export it.unibo.dap.controller.SerializableInstances.given_Serializable_String
    given Equatable[String] = _ == _

    @JSExport("Rule")
    class JSRule(pre: js.Array[Token], rate: Double, eff: js.Array[Token], msg: Token)
        extends Rule(MSet(pre.toList*), rate, MSet(eff.toList*), Option(msg))

    given Conversion[State[Token], JSState] = s => JSState(js.Array(s.tokens.elems*), s.msg.getOrElse(""))

    @JSExport("State")
    class JSState(tokens: js.Array[Token], msg: Token) extends State(MSet(tokens.toList*), Option(msg)):
      @JSExport("tokens") def jsTokens: js.Array[Token] = tokens
      @JSExport("msg") def jsMsg: Token = msg
end JSProductAPI
