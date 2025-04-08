package it.unibo.pingponger

import scala.scalajs.js
import scala.scalajs.js.annotation.*

@JSExportTopLevel("SocketAPI")
object SocketAPI:

  @JSExport
  def startServer(port: Int, onMessage: js.Function1[String, Unit]): Unit =
    SocketServer.start(port, onMessage)

  @JSExport
  def connectToServer(host: String, port: Int, onMessage: js.Function1[String, Unit]): js.Dynamic =
    val socket = SocketClient.connect(host, port, onMessage)
    js.Dynamic.literal(
      send = (msg: String) => socket.write(msg),
    )
