package it.unibo.dap.boundary.sockets

import java.net.{ ServerSocket, Socket }

import scala.collection.Iterator.continually
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

import it.unibo.dap.controller.Serializable
import it.unibo.dap.controller.Serializable.{ deserialize, serialize }

trait SocketNetworking[T: Serializable](using ExecutionContext) extends Networking[T, T] with InetTypes:

  override def out(endpoint: (Address, Port)): Future[Connection] = Future.fromTry:
    for
      socket <- Try(Socket(endpoint._1, endpoint._2))
      conn = new Connection:
        override def send(msg: T): Future[Unit] = Future.fromTry(Try(socket.getOutputStream.write(serialize(msg))))
        override def isOpen: Boolean = !socket.isClosed
        override def close(): Unit = socket.close()
    yield conn

  override def in(port: Port)(onReceive: T => Unit): Future[ConnectionListener] = Future.fromTry:
    for
      socketServer <- Try(ServerSocket(port))
      connListener = new ConnectionListener:
        private val acceptLoop = Future:
          continually(Try(socketServer.accept())).filter(_.isSuccess).foreach(s => serve(s.get))
        private def serve(client: Socket) = Future:
          val in = client.getInputStream
          val buffer = new Array[Byte](_length = 2048)
          continually(in.read(buffer))
            .takeWhile(_ > 0)
            .foreach: readBytes =>
              val message = deserialize(buffer.take(readBytes))
              onReceive(message)
          client.close()
        .recover { case _: Exception => if !client.isClosed then client.close() }
        override def isOpen: Boolean = socketServer.isClosed
        override def close(): Unit = socketServer.close()
    yield connListener
end SocketNetworking
