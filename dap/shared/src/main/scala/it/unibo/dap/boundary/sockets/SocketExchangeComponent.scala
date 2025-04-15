package it.unibo.dap.boundary.sockets

import it.unibo.dap.controller.{ ExchangeComponent, Serializable }
import it.unibo.dap.utils.{ Channel, ReadableChannel, SendableChannel }

import scala.concurrent.{ ExecutionContext, Future }

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
      Future.sequence(client() :: serveClients :: Nil).map(_ => ())

    private def client(connections: Map[Endpoint, Connection] = Map.empty)(using ExecutionContext): Future[Unit] =
      for
        msg <- outChannel.pop()
        neighbors <- Future.successful(ctx.neighbourhoodResolver())
        newConnections: Set[Either[Throwable, (Endpoint, Connection)]] <- Future.traverse(neighbors): n =>
          connections
            .get(n)
            .filter(_.isOpen)
            .map(c => Future.successful(n -> c))
            .getOrElse(establishConnection(n).map(n -> _))
            .flatMap((nc: (Endpoint, Connection)) => nc._2.send(msg).map(_ => nc))
            .map(Right(_))
            .recover { case e => Left(e) }
        _ <- client(newConnections.collect { case Right((n, c)) => n -> c }.toMap)
      yield ()

    private def establishConnection(endpoint: Endpoint): Future[Connection] = ctx.out(endpoint)

    private def serveClients(using ExecutionContext): Future[Unit] = ctx
      .in(port)(inChannel.push)
      .recover { case e => scribe.error(s"Error while starting socket server: ${e.getMessage}"); serveClients }
      .map(_ => scribe.info(s"Socket server bound to port $port"))
  end SocketExchange

end SocketExchangeComponent
