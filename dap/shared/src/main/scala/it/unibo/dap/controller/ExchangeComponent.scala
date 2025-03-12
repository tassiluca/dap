package it.unibo.dap.controller

import it.unibo.dap.utils.Spawnable

import gears.async.{ ReadableChannel, SendableChannel }

/** The component providing the [[Exchange]] of messages between neighbours.
  * @tparam T the type of the messages to exchange
  */
trait ExchangeComponent[T]:
  ctx: NeighbourhoodResolverComponent =>

  /** The exchange instance. */
  val exchange: Exchange

  trait Exchange extends Spawnable:
    /** The readable channel on which to receive messages from neighbours. */
    def inputs: ReadableChannel[T]

    /** The sendable source channel to send messages to neighbours. */
    def outputs: SendableChannel[T]
