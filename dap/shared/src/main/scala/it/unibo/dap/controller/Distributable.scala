package it.unibo.dap.controller

/** A type alias for a distributable state.
  * @tparam T the type of the message to distribute
  */
type DistributableState[T] = [S] =>> Distributable[S, T]

/** A type class for enriching a model state with distribution capabilities.
  * @tparam State the type of the state
  * @tparam Message the type of the message to distribute
  */
trait Distributable[State, Message]:

  extension (s: State)
    def msg: Option[Message]
    def updated(msg: Message): State
