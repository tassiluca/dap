package it.unibo.dap.boundary.sockets

import it.unibo.dap.controller.{ ExchangeComponent, Serializable }
import it.unibo.dap.utils.{ Channel, ReadableChannel, SendableChannel }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

/** An exchange that communicates with other nodes over plain sockets. */
trait SocketExchangeComponent[T: Serializable] extends ExchangeComponent[T]:
  ctx: InetNeighbourhoodResolver & Networking[T, T] =>

  /** The port on which the socket server listens for incoming connections. */
  def port: Port

  override val exchange: Exchange = SocketExchange(port)

  private class SocketExchange(port: Int) extends Exchange:
    private val inChannel = Channel[T]()
    private val outChannel = Channel[T]()

    override def inputs: ReadableChannel[T] = inChannel.asReadable

    override def outputs: SendableChannel[T] = outChannel.asSendable

    override def spawn(using ExecutionContext): Future[Unit] =
      Future.sequence(client() :: Future(serveClients) :: Nil).map(_ => ())

    private def client(connections: Map[Endpoint, Connection] = Map.empty)(using ExecutionContext): Future[Unit] =
      for
        msg <- outChannel.pop()
        newConnections =
          for
            (neighbour, conn) <- ctx
              .neighbourhoodResolver()
              .map(n => n -> connections.get(n).filter(_.isOpen).orElse(establishConnection(n)))
              .collect { case (e, Some(s)) => e -> s }
            nc <- conn.send(msg) match
              case Failure(e) => scribe.warn(e.getMessage); None
              case Success(_) => Some(neighbour -> conn)
          yield nc
        _ <- client(newConnections.toMap)
      yield ()

    private def establishConnection(endpoint: Endpoint): Option[Connection] = ctx.out(endpoint).toOption

    private def serveClients(using ExecutionContext): Unit = ctx.in(port)(inChannel.push) match
      case Failure(e) => scribe.error(s"Error while starting socket server: ${e.getMessage}")
      case Success(_) => scribe.info(s"Socket server bound to port $port")
  end SocketExchange

end SocketExchangeComponent
