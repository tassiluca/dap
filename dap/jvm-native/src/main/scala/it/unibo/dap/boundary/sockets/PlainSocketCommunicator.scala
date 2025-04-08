package it.unibo.dap.boundary.sockets

import it.unibo.dap.controller.Serializable
import it.unibo.dap.controller.Serializable.{ deserialize, serialize }

import java.net.{ ServerSocket, Socket }
import scala.collection.Iterator.continually
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

trait PlainSocketCommunicator[T: Serializable](using ExecutionContext) extends Communicator[T, T] with InetTypes:

  override def out(endpoint: (Address, Port)): OutChannel = new OutChannel:
    private val socket: Try[Socket] = Try(Socket(endpoint._1, endpoint._2))

    override def send(msg: T): Try[Unit] = socket.map(_.getOutputStream.write(serialize(msg)))

    override def isOpen: Boolean = socket.isSuccess && !socket.get.isClosed

    override def close(): Unit = socket.foreach(_.close())

  override def in(port: Port): InChannel = new InChannel:
    private var messageCallback: Option[T => Unit] = None
    private val socketServer: Try[ServerSocket] = Try(ServerSocket(port))

    Future(setup())

    private def setup(): Unit = socketServer.foreach: s =>
      scribe.info(s"Socket server bound to port $port")
      continually(s.accept()).foreach(s => Future(serve(s)))

    private def serve(client: Socket): Unit =
      scribe.info(s"New connection from ${client.getInetAddress}")
      val in = client.getInputStream
      val buffer = new Array[Byte](_length = 2048)
      continually(in.read(buffer))
        .takeWhile(_ > 0)
        .foreach: readBytes =>
          val message = deserialize(buffer.take(readBytes))
          messageCallback.foreach(_(message))
      client.close()

    override def onReceive(callback: T => Unit): Unit = synchronized:
      messageCallback = Some(callback)

    override def isOpen: Boolean = socketServer.isSuccess && !socketServer.get.isClosed

    override def close(): Unit = socketServer.foreach(_.close())
end PlainSocketCommunicator
