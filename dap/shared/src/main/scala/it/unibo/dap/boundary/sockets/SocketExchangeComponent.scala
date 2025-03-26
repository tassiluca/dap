package it.unibo.dap.boundary.sockets

import gears.async.*
import it.unibo.dap.boundary.Serializable
import it.unibo.dap.boundary.Serializable.*
import it.unibo.dap.controller.ExchangeComponent

import java.net.{ ServerSocket, Socket }
import scala.annotation.tailrec
import scala.collection.Iterator.continually
import scala.util.{ Failure, Success, Try }

/** An exchange that communicates with other exchanges over plain sockets. */
trait SocketExchangeComponent[T: Serializable] extends ExchangeComponent[T]:
  ctx: InetNeighbourhoodResolver =>

  def port: Port

  override val exchange: Exchange = SocketExchange(port)

  private class SocketExchange(port: Int) extends Exchange:
    private lazy val inChannel = BufferedChannel[T](size = 10)
    private lazy val outChannel = BufferedChannel[T](size = 10)

    override def inputs: ReadableChannel[T] = inChannel.asReadable

    override def outputs: SendableChannel[T] = outChannel.asSendable

    override def start(using Async, AsyncOperations): Unit = Async.group:
      Future(client())
      serveClients()

    @tailrec
    private def client(connections: Map[Endpoint, Socket] = Map.empty)(using Async): Unit =
      outChannel.read() match
        case Left(_) => ()
        case Right(message) =>
          scribe.info(s"[Sim exch] Sending message $message to neighbours")
          val bytes = serialize(message)
          val newConnections =
            for
              (n, s) <- ctx
                .neighbourhoodResolver()
                .map(n => n -> connections.get(n).filterNot(_.isClosed).orElse(establishConnection(n)))
                .collect { case (e, Some(s)) => e -> s }
              c <- Try(s.getOutputStream.write(bytes)) match
                case Failure(e) => scribe.error(e); None
                case Success(_) => Some(n -> s)
            yield c
          client(newConnections.toMap)

    private def establishConnection(endpoint: Endpoint): Option[Socket] =
      Try(Socket(endpoint._1, endpoint._2)).toOption

    private def serveClients()(using Async.Spawn): Unit =
      val server = ServerSocket(port)
      scribe.info(s"Socket server bound to port $port")
      continually(server.accept()).foreach: client =>
        scribe.debug(s"Accepted connection from ${client.getInetAddress}")
        Future(serve(client))

    private def serve(client: Socket)(using Async): Unit =
      val in = client.getInputStream
      val buffer = new Array[Byte](1024)
      continually(in.read(buffer))
        .takeWhile(_ > 0)
        .foreach: readBytes =>
          scribe.info("[Sim exch] Received message from neighbour")
          val message = deserialize(buffer.take(readBytes))
          inChannel.send(message)
      client.close()
  end SocketExchange

end SocketExchangeComponent
