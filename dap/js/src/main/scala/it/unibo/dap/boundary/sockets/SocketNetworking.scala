package it.unibo.dap.boundary.sockets

import it.unibo.dap.boundary.sockets.{ InetTypes, Networking }
import it.unibo.dap.controller.Serializable
import it.unibo.dap.controller.Serializable.{ deserialize, serialize }

import scala.scalajs.js.typedarray.Uint8Array
import scala.util.Try

trait SocketNetworking[T: Serializable] extends Networking[T, T] with InetTypes:

  override def out(endpoint: Endpoint): Try[Connection] = ???
//    new Connection:
//      private val socket = SocketClient.connect(endpoint._1, endpoint._2, _ => ())
//
//      override def send(msg: T): Try[Unit] = Try:
//        val bytes = serialize(msg)
//        val data = Uint8Array(bytes.length)
//        for i <- bytes.indices do data(i) = bytes(i)
//        socket.write(data)
//
//      override def isOpen: Boolean = !socket.destroyed
//
//      override def close(): Unit = socket.destroy()

  override def in(port: Port): Try[ConnectionListener] = ???
//    new ConnectionListener:
//      private var msgCallback: Option[T => Unit] = None
//      private var open = true
//      private val server = SocketServer.start(
//        port,
//        data =>
//          val bytes = new Array[Byte](data.length)
//          for i <- 0 until data.length do bytes(i) = data(i).toByte
//          val msg = deserialize(bytes)
//          msgCallback.foreach(_(msg)),
//      )
//
//      override def onReceive(callback: T => Unit): Unit = msgCallback = Some(callback)
//
//      override def isOpen: Boolean = open
//
//      override def close(): Unit =
//        open = false
//        server.close()
end SocketNetworking
