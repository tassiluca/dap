package it.unibo.dap.boundary.sockets

import it.unibo.dap.controller.{ ExchangeComponent, Serializable }
import it.unibo.dap.utils.{ Channel, ReadableChannel, SendableChannel }

import scala.concurrent.{ ExecutionContext, Future }

/** An exchange that communicates with other nodes over plain sockets. */
trait SocketExchangeComponent[T: Serializable] extends ExchangeComponent[T]:
  ctx: InetNeighborhoodResolver & Networking[T, T] =>

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
        neighbors <- Future.successful(ctx.neighborhoodResolver())
        newConnections <- Future.traverse(neighbors): n =>
          connections
            .get(n)
            .filter(_.isOpen)
            .fold(establishConnection(n))(Future.successful)
            .flatMap(c => c.send(msg).map(_ => Right(n -> c)))
            .recover { case e => Left(e) }
        _ <- client(newConnections.collect { case Right(nc) => nc }.toMap)
      yield ()

    private def establishConnection(endpoint: Endpoint): Future[Connection] = ctx.out(endpoint)

    private def serveClients(using ExecutionContext): Future[Unit] = ctx
      .in(port)(inChannel.push)
      .recover { case e => scribe.error(s"Error while starting socket server: ${e.getMessage}"); serveClients }
      .map(_ => scribe.info(s"Socket server bound to port $port"))
  end SocketExchange

end SocketExchangeComponent
