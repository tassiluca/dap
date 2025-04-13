//package it.unibo.dap.api
//
//import it.unibo.dap.modelling.Equatable
//import it.unibo.dap.utils.as
//
//import scala.concurrent.ExecutionContext
//import scala.scalajs.concurrent.JSExecutionContext
//import scala.scalajs.js
//import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportTopLevel}
//
//@JSExportTopLevel("ProductAPI")
//object JSProductAPI extends ProductAPI:
//
//  override val interface: Interface = JSInterface
//
//  @JSExportAll
//  @JSExport
//  object JSInterface extends ProductInterface with JSADTs:
//    override given ExecutionContext = JSExecutionContext.queue
//
//    def launchSimulation(
//        rules: js.Array[JSRule],
//        initial: JSState,
//        port: Int,
//        neighbourhood: js.Array[Neighbour],
//        onStateChange: js.Function1[JSState, Unit],
//    ): Unit =
//      setup()
//      scribe.info(s"Rules are: ${rules}")
//      scribe.info(s"Initial state is: ${initial}")
//      scribe.info(s"Neighbourhood is: [${neighbourhood}]")
//      scribe.info(s"Port is: ${port}")
//      onStateChange(JSState(js.Array(), ""))
//      simulate(rules.toSet, initial, onStateChange(_))(port, neighbourhood.toSet)
//
//    private def setup(): Unit =
//      interface.registerSerDe[Token](_.getBytes, new String(_))
//      interface.registerEquatable[Token](_ == _)
//
//    override def simulate(rules: Set[Rule], initial: State, updateFn: State => Unit)(
//        port: Int,
//        neighbours: Set[Neighbour],
//    ): Unit =
//      import it.unibo.dap.controller.SerializableInstances.given_Serializable_String
//      given Equatable[String] = _ == _
//      DAPSimulation(initial.as, rules.map(rCvt))(port, neighbours).launch(updateFn)
//
//  end JSInterface
//
//  trait JSADTs extends ADTs:
//    override type Token = String
//
//    @JSExport("Rule")
//    class JSRule(pre: js.Array[Token], rate: Double, eff: js.Array[Token], msg: Token)
//        extends Rule(MSet(pre.toList*), rate, MSet(eff.toList*), Option(msg))
//
//    given Conversion[State, JSState] = s => JSState(js.Array(s.tokens.elems*), s.msg.getOrElse(""))
//
//    @JSExport("State")
//    class JSState(tokens: js.Array[Token], msg: Token) extends State(MSet(tokens.toList*), Option(msg)):
//      @JSExport("tokens") def jsTokens: js.Array[Token] = tokens
//      @JSExport("msg") def jsMsg: Token = msg
//end JSProductAPI
