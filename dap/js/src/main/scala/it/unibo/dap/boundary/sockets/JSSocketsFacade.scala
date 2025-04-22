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
  def on(event: String)(callback: js.Function1[js.Any, Unit]): Unit = js.native

@js.native
trait Socket extends js.Object:
  def write(data: Uint8Array)(callback: js.Function1[Error | Null, Any]): Unit = js.native
  def on(event: String)(callback: js.Function1[js.Any, Unit]): Unit = js.native
  def destroy(): Unit = js.native
  def destroyed: Boolean = js.native
