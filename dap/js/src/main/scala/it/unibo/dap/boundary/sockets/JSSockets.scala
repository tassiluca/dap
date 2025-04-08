package it.unibo.dap.boundary.sockets

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.typedarray.Uint8Array

@js.native
@JSImport("net", JSImport.Namespace)
object Net extends js.Object:
  def connect(port: Int, host: String): Socket = js.native

  def createServer(cb: js.Function1[Socket, Unit]): Server = js.native

@js.native
trait Server extends js.Object:
  def listen(port: Int): Unit = js.native

  def close(): Unit = js.native

@js.native
trait Socket extends js.Object:
  def write(data: String): Unit = js.native
  def write(data: Uint8Array): Unit = js.native
  def on(event: String, callback: js.Function1[js.Any, Unit]): Unit = js.native
  def destroy(): Unit = js.native
  def destroyed: Boolean = js.native

object SocketClient:
  def connect(host: String, port: Int, onMessage: js.typedarray.Uint8Array => Unit): Socket =
    val socket = Net.connect(port, host)
    socket.on("data", (data: js.Any) =>
      val buffer = data.asInstanceOf[Uint8Array]
      onMessage(buffer)
    )
    socket

object SocketServer:
  def start(port: Int, onMessage: js.typedarray.Uint8Array => Unit): Server =
    val server = Net.createServer((socket: Socket) =>
      socket.on("data", (data: js.Any) =>
        val buffer = data.asInstanceOf[Uint8Array]
        onMessage(buffer)
      )
    )
    server.listen(port)
    server

