package it.unibo.dap.boundary

import gears.async.{ Async, AsyncOperations, BufferedChannel, Future, ReadableChannel, SendableChannel, Task }
import it.unibo.dap.controller.Exchange
import it.unibo.dap.utils.Spawnable

import java.net.{ ServerSocket, Socket }
import scala.annotation.tailrec
import scala.util.{ Failure, Success, Try }

object SocketExchange:

  type Endpoint = (Address, Port)
  type Address = String
  type Port = Int

  def apply(port: Port, net: Set[Endpoint]): Exchange[String] & Spawnable =
    new SocketExchangeImpl(port, net) with Spawnable:
      override def start(using Async, AsyncOperations): Unit = startService

  private class SocketExchangeImpl(port: Port, net: Set[Endpoint]) extends Exchange[String]:

    private lazy val inChannel = BufferedChannel[String](size = 50)
    private lazy val outChannel = BufferedChannel[String](size = 50)

    override def outputs: SendableChannel[String] = outChannel.asSendable

    override def inputs: ReadableChannel[String] = inChannel.asReadable

    def startService(using Async, AsyncOperations): Unit = Async.group:
      Task(client()).start()
      serveClients

    @tailrec
    private def client(connections: Map[Endpoint, Socket] = Map.empty)(using Async): Unit =
      outChannel.read() match
        case Left(_) => ()
        case Right(message) =>
          scribe.debug(s"Sending message: $message")
          val all = net
            .map(e => e -> connections.get(e).filterNot(_.isClosed).orElse(establishConnection(e)))
            .collect { case (e, Some(s)) => e -> s }
          val newConnections = all.flatMap:
            case (e, s) =>
              Try(s.getOutputStream.write(message.getBytes)) match
                case Success(_) => Some(e -> s)
                case Failure(e) => scribe.error(e); None
          client(newConnections.toMap)

    private def serveClients(using Async.Spawn): Unit =
      val server = ServerSocket(port)
      scribe.info(s"Socket server bound to port $port")
      while !server.isClosed do
        val client = server.accept()
        scribe.debug(s"Accepted connection from ${client.getInetAddress}")
        Future(serve(client))

    private def serve(client: Socket)(using Async): Unit =
      val in = client.getInputStream
      val buffer = new Array[Byte](1024)
      while true do
        val readBytes = in.read(buffer)
        if readBytes > 0 then
          val message = new String(buffer, 0, readBytes)
          scribe.debug(s"Received message: $message")
          inChannel.send(message)
        else
          scribe.debug(s"Connection closed with ${client.getInetAddress}")
          client.close()
          return

    private def establishConnection(endpoint: Endpoint): Option[Socket] = Try(Socket(endpoint._1, endpoint._2)).toOption
  end SocketExchangeImpl
end SocketExchange
