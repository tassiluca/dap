package it.unibo.dap.boundary.sockets

import scala.concurrent.Future

/** High-level, platform-independent networking interface.
  * @tparam MessageIn the type of the messages received from the network
  * @tparam MessageOut the type of the messages sent over the network
  */
trait Networking[+MessageIn, -MessageOut]:
  self: InetTypes =>

  /** Creates an outgoing [[Connection]] to the given [[endpoint]]. */
  def out(endpoint: Endpoint): Future[Connection]

  /** Creates an incoming [[ConnectionListener]] on the given [[port]]. */
  def in(port: Port)(onReceive: MessageIn => Unit): Future[ConnectionListener]

  /** A closable outbound connection to a remote endpoint through which messages flow. */
  trait Connection extends AutoCloseable:
    def send(msg: MessageOut): Future[Unit]
    def isOpen: Boolean

  /** A closable inbound listener for incoming messages from remote endpoints. */
  trait ConnectionListener extends AutoCloseable:
    def isOpen: Boolean
