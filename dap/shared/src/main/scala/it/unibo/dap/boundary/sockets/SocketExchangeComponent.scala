package it.unibo.dap.boundary.sockets

import it.unibo.dap.controller.Serializable
import it.unibo.dap.controller.ExchangeComponent
import it.unibo.dap.utils.{ Channel, ReadableChannel, SendableChannel }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

/** An exchange that communicates with other exchanges over plain sockets. */
trait SocketExchangeComponent[T: Serializable] extends ExchangeComponent[T]:
  ctx: InetNeighbourhoodResolver & Communicator[T, T] =>

  def port: Port

  override val exchange: Exchange = SocketExchange(port)

  private class SocketExchange(port: Int) extends Exchange:
    private lazy val inChannel = Channel[T]()
    private lazy val outChannel = Channel[T]()

    override def inputs: ReadableChannel[T] = inChannel.asReadable

    override def outputs: SendableChannel[T] = outChannel.asSendable

    override def spawn(using ExecutionContext): Future[Unit] =
      Future.sequence(Future(client()) :: Future(serveClients) :: Nil).map(_ => ())

    private def client(connections: Map[Endpoint, OutChannel] = Map.empty)(using ExecutionContext): Unit =
      outChannel
        .pop()
        .onComplete:
          case Failure(e) =>
            scribe.error(s"Error while sending message: $e")
            client(connections)
          case Success(message) =>
            val newConnections =
              for
                (n, conn) <- ctx
                  .neighbourhoodResolver()
                  .map(n => n -> connections.get(n).filter(_.isOpen).orElse(establishConnection(n)))
                  .collect { case (e, Some(s)) => e -> s }
                c <- conn.send(message) match
                  case Failure(e) => scribe.warn(e.getMessage); None
                  case Success(_) => Some(n -> conn)
              yield c
            client(newConnections.toMap)

    private def establishConnection(endpoint: Endpoint): Option[OutChannel] = Try(ctx.out(endpoint)).toOption

    private def serveClients(using ExecutionContext): Unit =
      val server = ctx.in(port)
      scribe.info(s"Socket server bound to port $port")
      server.onReceive(inChannel.push)
  end SocketExchange

end SocketExchangeComponent
