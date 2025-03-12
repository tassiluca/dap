package it.unibo.dap.controller

/** A type alias for a [[Distributable]] state. */
type DistributableState[Message] = [State] =>> Distributable[State, Message]

/** A type class for enriching a model state with distribution capabilities.
  * @tparam State the type of the state
  * @tparam Message the type of the message to distribute
  */
trait Distributable[State, Message]:

  extension (s: State)
    def msg: Option[Message]
    def updated(msg: Message): State
