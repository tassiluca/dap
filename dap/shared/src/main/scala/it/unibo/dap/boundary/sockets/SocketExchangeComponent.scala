package it.unibo.dap.boundary.sockets

import scala.concurrent.{ ExecutionContext, Future }

import it.unibo.dap.utils.*
import it.unibo.dap.controller.ExchangeComponent

/** An exchange that communicates with other nodes over plain sockets. */
trait SocketExchangeComponent[T] extends ExchangeComponent[T]:
  ctx: InetNeighborhoodResolver & Networking[T, T] =>

  /** The port on which the socket server listens for incoming connections. */
  override type Configuration = Port

  override val exchange: Exchange = SocketExchange()

  private class SocketExchange extends Exchange with AutoCloseable:
    private var connectionListener = Option.empty[ConnectionListener]
    private val inChannel = Channel[T]()
    private val outChannel = Channel[T]()

    override def inputs: ReadableChannel[T] = inChannel.asReadable

    override def outputs: SendableChannel[T] = outChannel.asSendable

    override def spawn(configuration: Configuration)(using ExecutionContext): Future[Unit] =
      val tasks = client().recover { case _: Channel.ClosedException => () } :: serveClients(configuration) :: Nil
      Future.sequence(tasks).unit

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

    private def serveClients(configuration: Configuration)(using ExecutionContext): Future[Unit] = ctx
      .in(configuration)(inChannel.push)
      .map: c =>
        scribe.info(f"Socket server listening on port $configuration")
        connectionListener = Some(c)
      .recoverWith { case e => scribe.error(s"Socket server error: ${e.getMessage}"); serveClients(configuration) }

    override def close(): Unit =
      inChannel.close()
      outChannel.close()
      connectionListener.foreach(_.close())
  end SocketExchange

end SocketExchangeComponent
