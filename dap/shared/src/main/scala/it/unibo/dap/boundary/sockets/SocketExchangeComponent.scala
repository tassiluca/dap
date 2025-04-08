package it.unibo.dap.boundary.sockets

import it.unibo.dap.controller.Serializable
import it.unibo.dap.controller.ExchangeComponent
import it.unibo.dap.utils.Task

import java.util.concurrent.{ BlockingQueue, LinkedBlockingQueue }
import scala.annotation.tailrec
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

/** An exchange that communicates with other exchanges over plain sockets. */
trait SocketExchangeComponent[T: Serializable] extends ExchangeComponent[T]:
  ctx: InetNeighbourhoodResolver & Communicator[T, T] =>

  def port: Port

  override val exchange: Exchange = SocketExchange(port)

  private class SocketExchange(port: Int) extends Exchange:
    private lazy val inChannel = LinkedBlockingQueue[T]()
    private lazy val outChannel = LinkedBlockingQueue[T]()

    override def inputs: BlockingQueue[T] = inChannel

    override def outputs: BlockingQueue[T] = outChannel

    override def spawn(using ExecutionContext): Task[Unit] = () =>
      Future.sequence(Future(client()) :: Future(serveClients) :: Nil).map(_ => ())

    @tailrec
    private def client(connections: Map[Endpoint, OutChannel] = Map.empty): Unit =
      val message = outChannel.take()
      val newConnections =
        for
          (n, conn) <- ctx
            .neighbourhoodResolver()
            .map(n => n -> connections.get(n).filter(_.isOpen).orElse(establishConnection(n)))
            .collect { case (e, Some(s)) => e -> s }
          c <- conn.send(message) match
            case Failure(e) => scribe.error(e); None
            case Success(_) => Some(n -> conn)
        yield c
      client(newConnections.toMap)

    private def establishConnection(endpoint: Endpoint): Option[OutChannel] = Try(ctx.out(endpoint)).toOption

    private def serveClients(using ExecutionContext): Unit =
      val server = ctx.in(port)
      scribe.info(s"Socket server bound to port $port")
      server.onReceive(inChannel.offer)
  end SocketExchange

end SocketExchangeComponent
