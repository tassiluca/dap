package it.unibo.dap.boundary.sockets

import it.unibo.dap.boundary.Serializable
import it.unibo.dap.boundary.Serializable.*
import it.unibo.dap.controller.ExchangeComponent
import it.unibo.dap.utils.Task

import java.net.{ ServerSocket, Socket }
import java.util.concurrent.{ BlockingQueue, LinkedBlockingQueue }
import scala.annotation.tailrec
import scala.collection.Iterator.continually
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

/** An exchange that communicates with other exchanges over plain sockets. */
trait SocketExchangeComponent[T: Serializable] extends ExchangeComponent[T]:
  ctx: InetNeighbourhoodResolver =>

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
    private def client(connections: Map[Endpoint, Socket] = Map.empty): Unit =
      val message = outChannel.take()
      val newConnections =
        for
          (n, s) <- ctx
            .neighbourhoodResolver()
            .map(n => n -> connections.get(n).filterNot(_.isClosed).orElse(establishConnection(n)))
            .collect { case (e, Some(s)) => e -> s }
          c <- Try(s.getOutputStream.write(serialize(message))) match
            case Failure(e) => scribe.error(e); None
            case Success(_) => Some(n -> s)
        yield c
      client(newConnections.toMap)

    private def establishConnection(endpoint: Endpoint): Option[Socket] =
      Try(Socket(endpoint._1, endpoint._2)).toOption

    private def serveClients(using ExecutionContext): Unit =
      val server = ServerSocket(port)
      scribe.info(s"Socket server bound to port $port")
      continually(server.accept()).foreach: client =>
        scribe.debug(s"Accepted connection from ${client.getInetAddress}")
        Future(serve(client))

    private def serve(client: Socket): Unit =
      val in = client.getInputStream
      val buffer = new Array[Byte](1024)
      continually(in.read(buffer))
        .takeWhile(_ > 0)
        .foreach: readBytes =>
          val message = deserialize(buffer.take(readBytes))
          inChannel.offer(message)
      client.close()
  end SocketExchange

end SocketExchangeComponent
