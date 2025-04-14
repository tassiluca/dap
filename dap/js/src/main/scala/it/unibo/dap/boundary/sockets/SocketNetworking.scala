package it.unibo.dap.boundary.sockets

import it.unibo.dap.controller.Serializable
import it.unibo.dap.controller.Serializable.{ deserialize, serialize }

import scala.concurrent.{ Await, ExecutionContext, Promise }
import scala.scalajs.js.typedarray.Uint8Array
import scala.util.{ Failure, Try }
import scala.concurrent.duration.DurationInt

trait SocketNetworking[T: Serializable](using ExecutionContext) extends Networking[T, T] with InetTypes:

  override def out(endpoint: Endpoint): Try[Connection] =
    def createConnection(socket: Socket) = new Connection:
      override def send(msg: T): Try[Unit] = Try:
        val bytes = serialize(msg)
        val data = Uint8Array(bytes.length)
        for i <- bytes.indices do data(i) = bytes(i)
        socket.write(data)
      override def isOpen: Boolean = !socket.destroyed
      override def close(): Unit = socket.destroy()
    val promiseConn = Promise[Connection]()
    val socket = Net.connect(endpoint._2, endpoint._1)
    socket.on("connect", _ => promiseConn.trySuccess(createConnection(socket)))
    socket.on("error", err => promiseConn.tryFailure(new Exception(err.toString)))
    Try(Await.result(promiseConn.future, 5.seconds)).recoverWith { case ex => socket.destroy(); Failure(ex) }

  override def in(port: Port)(onReceive: T => Unit): Try[ConnectionListener] =
    val promiseConnListener = Promise[ConnectionListener]()
    def react(data: Uint8Array): Unit =
      val bytes = new Array[Byte](data.length)
      for i <- 0 until data.length do bytes(i) = data(i).toByte
      onReceive(deserialize(bytes))
    val server = Net.createServer(socket => socket.on("data", data => react(data.asInstanceOf[Uint8Array])))
    val listener = new ConnectionListener:
      private var open = true
      override def isOpen: Boolean = open
      override def close(): Unit = open = false; server.close()
    server.on("listening", _ => promiseConnListener.trySuccess(listener))
    Try(Await.result(promiseConnListener.future, 5.seconds)).recoverWith { case ex => listener.close(); Failure(ex) }
end SocketNetworking
