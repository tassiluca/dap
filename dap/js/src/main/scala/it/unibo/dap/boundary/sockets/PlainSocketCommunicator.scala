package it.unibo.dap.boundary.sockets

import it.unibo.dap.boundary.sockets.{ Communicator, InetTypes }
import it.unibo.dap.controller.Serializable
import it.unibo.dap.controller.Serializable.{ deserialize, serialize }

import scala.scalajs.js.typedarray.Uint8Array
import scala.util.Try

trait PlainSocketCommunicator[T: Serializable] extends Communicator[T, T] with InetTypes:

  override def out(endpoint: Endpoint): OutChannel = new OutChannel:
    private val socket = SocketClient.connect(endpoint._1, endpoint._2, _ => ())

    override def send(msg: T): Try[Unit] = Try:
      val bytes = serialize(msg)
      val data = Uint8Array(bytes.length)
      for i <- bytes.indices do data(i) = bytes(i)
      socket.write(data)

    override def isOpen: Boolean = !socket.destroyed

    override def close(): Unit = socket.destroy()

  override def in(port: Port): InChannel = new InChannel:
    private var msgCallback: Option[T => Unit] = None
    private var open = true
    private val server = SocketServer.start(
      port,
      data =>
        val bytes = new Array[Byte](data.length)
        for i <- 0 until data.length do bytes(i) = data(i).toByte
        val msg = deserialize(bytes)
        msgCallback.foreach(_(msg)),
    )

    override def onReceive(callback: T => Unit): Unit = msgCallback = Some(callback)

    override def isOpen: Boolean = open

    override def close(): Unit =
      open = false
      server.close()
end PlainSocketCommunicator
