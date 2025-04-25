package it.unibo.dap.controller

import it.unibo.dap.utils.{ ReadableChannel, SendableChannel, Spawnable }

/** The component providing the [[Exchange]] of messages between neighbors.
  * @tparam T the type of the messages to exchange
  */
trait ExchangeComponent[T]:
  ctx: NeighborhoodResolverComponent =>

  /** The required configuration to set up the exchange. */
  type Configuration

  /** The exchange instance. */
  val exchange: Exchange

  trait Exchange extends Spawnable[Configuration] with AutoCloseable:
    /** The readable channel on which to receive messages from neighbors. */
    def inputs: ReadableChannel[T]

    /** The sendable source channel to send messages to neighbors. */
    def outputs: SendableChannel[T]
