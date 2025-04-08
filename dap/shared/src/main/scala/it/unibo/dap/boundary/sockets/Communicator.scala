package it.unibo.dap.boundary.sockets

import it.unibo.dap.controller.Serializable
import scala.util.Try

trait Communicator[-In: Serializable, +Out: Serializable]:
  self: InetTypes =>

  def out(endpoint: Endpoint): OutChannel

  def in(port: Port): InChannel

  trait OutChannel extends AutoCloseable:
    def send(msg: In): Try[Unit]
    def isOpen: Boolean

  trait InChannel extends AutoCloseable:
    def onReceive(callback: Out => Unit): Unit
    def isOpen: Boolean
