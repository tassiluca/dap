package it.unibo.pingponger

import scala.scalajs.js
import scala.scalajs.js.annotation.*

@js.native
@JSImport("net", JSImport.Namespace)
object NetClient extends js.Object:
  def connect(port: Int, host: String): Socket = js.native

object SocketClient:

  def connect(host: String, port: Int, onMessage: String => Unit): Socket =
    val socket = NetClient.connect(port, host)
    socket.on("data", (data: js.Any) => onMessage(data.toString))
    socket
