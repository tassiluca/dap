package it.unibo.dap.boundary

import gears.async.*
import it.unibo.dap.controller.{ Exchange, InetNeighbourhoodSensor }

import java.net.{ ServerSocket, Socket }
import scala.annotation.tailrec
import scala.collection.Iterator.continually
import scala.util.{ Failure, Success, Try }

/** An exchange that communicates with other exchanges over plain sockets.
  *
  * @param port the port to bind the server socket to
  */
trait SocketExchange(port: Int) extends Exchange[String]:
  neighborhoodSensor: InetNeighbourhoodSensor =>

  private lazy val inChannel = BufferedChannel[String](size = 10)
  private lazy val outChannel = BufferedChannel[String](size = 10)

  override def inputs: ReadableChannel[String] = inChannel.asReadable

  override def outputs: SendableChannel[String] = outChannel.asSendable

  override def start(using Async, AsyncOperations): Unit = Async.group:
    Task(client()).start()
    serveClients(port)

  @tailrec
  private def client(connections: Map[Endpoint, Socket] = Map.empty)(using Async): Unit =
    outChannel.read() match
      case Left(_) => ()
      case Right(message) =>
        scribe.debug(s"Sending message: $message")
        val newConnections = for
          (n, s) <- neighborhoodSensor()
            .map(n => n -> connections.get(n).filterNot(_.isClosed).orElse(establishConnection(n)))
            .collect { case (e, Some(s)) => e -> s }
          c <- Try(s.getOutputStream.write(message.getBytes)) match
            case Failure(e) => scribe.error(e); None
            case Success(_) => Some(n -> s)
        yield c
        client(newConnections.toMap)

  private def establishConnection(endpoint: Endpoint): Option[Socket] =
    Try(Socket(endpoint._1, endpoint._2)).toOption

  private def serveClients(port: Port)(using Async.Spawn): Unit =
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
        val message = new String(buffer, 0, readBytes)
        scribe.debug(s"Received message: $message")
        inChannel.send(message)
    client.close()

end SocketExchange
