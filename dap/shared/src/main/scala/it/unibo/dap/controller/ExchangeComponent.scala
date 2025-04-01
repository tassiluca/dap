package it.unibo.dap.controller

import it.unibo.dap.utils.Spawnable

/** The component providing the [[Exchange]] of messages between neighbours.
  * @tparam T the type of the messages to exchange
  */
trait ExchangeComponent[T]:
  ctx: NeighbourhoodResolverComponent =>

  /** The exchange instance. */
  val exchange: Exchange

  trait Exchange extends Spawnable:
    import java.util.concurrent.BlockingQueue

    /** The readable channel on which to receive messages from neighbours. */
    def inputs: BlockingQueue[T]

    /** The sendable source channel to send messages to neighbours. */
    def outputs: BlockingQueue[T]
