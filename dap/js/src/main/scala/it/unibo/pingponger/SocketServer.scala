package it.unibo.pingponger

import scala.scalajs.js
import scala.scalajs.js.annotation.*

@js.native
@JSImport("net", JSImport.Namespace)
object Net extends js.Object:
  def createServer(cb: js.Function1[Socket, Unit]): Server = js.native

@js.native
trait Server extends js.Object:
  def listen(port: Int): Unit = js.native

@js.native
trait Socket extends js.Object:
  def write(data: String): Unit = js.native
  def on(event: String, callback: js.Function1[js.Any, Unit]): Unit = js.native

object SocketServer:

  def start(port: Int, onMessage: String => Unit): Unit =
    val server = Net.createServer((socket: Socket) => socket.on("data", (data: js.Any) => onMessage(data.toString)))
    server.listen(port)
