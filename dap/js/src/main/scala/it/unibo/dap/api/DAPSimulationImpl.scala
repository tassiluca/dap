//package it.unibo.dap.api
//
//import it.unibo.dap.boundary.sockets.{InetNeighbourhoodResolver, PlainSocketCommunicator, SocketExchangeComponent}
//import it.unibo.dap.controller.DistributableInstances.given
//import it.unibo.dap.controller.{DAPSimulation, Serializable}
//import it.unibo.dap.modelling.DAP.*
//import it.unibo.dap.modelling.{CTMC, DAP, Equatable}
//
//import scala.concurrent.ExecutionContext
//
//object DAPSimulation:
//
//  def apply[Token: {Equatable, Serializable}](s0: DAP.State[Token], rules: Set[DAP.Rule[Token]])(
//      port: Int,
//      neighbors: Set[String],
//  )(using ExecutionContext): DAPSimulation[Token] = DAPSimulationImpl(s0, rules)(port, neighbors)
//
//  private class DAPSimulationImpl[Token: {Equatable, Serializable}](
//      s0: State[Token],
//      rules: Set[Rule[Token]],
//  )(exchPort: Int, neighbors: Set[String])(using ExecutionContext)
//      extends DAPSimulation
//      with SocketExchangeComponent
//      with InetNeighbourhoodResolver
//      with PlainSocketCommunicator[Token]:
//
//    override def initial: State[Token] = s0
//    override def behavior: CTMC[State[Token]] = DAP(rules)
//    override def port: Port = exchPort
//
//    override val neighbourhoodResolver: NeighbourhoodResolver = NeighbourhoodResolver.static:
//      neighbors.map:
//        case s"$address:$port" => (address, port.toInt)
//        case _ => throw new IllegalArgumentException("Invalid address:port format")
//end DAPSimulation
