package it.unibo.dap.boundary.sockets

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.scalajs.js.typedarray.Uint8Array

import it.unibo.dap.controller.Serializable
import it.unibo.dap.controller.Serializable.{ deserialize, serialize }

trait SocketNetworking[T: Serializable](using ExecutionContext) extends Networking[T, T] with InetTypes:

  override def out(endpoint: Endpoint): Future[Connection] =
    def createConnection(socket: Socket) = new Connection:
      override def send(msg: T): Future[Unit] =
        if !isOpen then Future.failed(IllegalStateException("Connection is closed!"))
        val promise = Promise[Unit]()
        val bytes = serialize(msg)
        val data = Uint8Array(bytes.length)
        for i <- bytes.indices do data(i) = bytes(i)
        socket.write(data): err =>
          if err != null then promise.tryFailure(new Exception(err.toString)) else promise.trySuccess(())
        promise.future
      override def isOpen: Boolean = !socket.destroyed
      override def close(): Unit = socket.destroy()
    val promiseConn = Promise[Connection]()
    val socket = Net.connect(endpoint._2, endpoint._1)
    socket.on("connect")(_ => promiseConn.trySuccess(createConnection(socket)))
    socket.on("error"): err =>
      socket.destroy()
      promiseConn.tryFailure(new Exception(err.toString))
    promiseConn.future
  end out

  override def in(port: Port)(onReceive: T => Unit): Future[ConnectionListener] =
    val promiseConnListener = Promise[ConnectionListener]()
    def react(data: Uint8Array): Unit =
      val bytes = new Array[Byte](data.length)
      for i <- 0 until data.length do bytes(i) = data(i).toByte
      onReceive(deserialize(bytes))
    val server = Net.createServer(socket => socket.on("data")(data => react(data.asInstanceOf[Uint8Array])))
    val listener = new ConnectionListener:
      private var open = true
      override def isOpen: Boolean = open
      override def close(): Unit = open = false; server.close()
    server.on("listening"): _ =>
      scribe.info("Socket server is now listening...")
      promiseConnListener.trySuccess(listener)
    server.on("error"): err =>
      server.close()
      promiseConnListener.tryFailure(new Exception(err.toString))
    server.listen(port)
    promiseConnListener.future
end SocketNetworking
