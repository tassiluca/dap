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

object DistributableInstances:
  import it.unibo.dap.modelling.{ DAP, MSet }

  given [T: Serializable] => Distributable[DAP.State[T], T] =
    new Distributable[DAP.State[T], T]:
      extension (s: DAP.State[T])
        override def msg: Option[T] = s.msg
        override def updated(msg: T): DAP.State[T] = s.copy(tokens = s.tokens union MSet(msg))
