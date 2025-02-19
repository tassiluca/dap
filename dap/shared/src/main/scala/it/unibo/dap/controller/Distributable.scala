package it.unibo.dap.controller

type DistributableState[T] = [S] =>> Distributable[S, T]

trait Distributable[State, Message]:
  extension (s: State)
    def msg: Message
    def updated(msg: Message): State

object DistributableInstances:

  import it.unibo.dap.modelling.DAP
  import it.unibo.dap.utils.MSet

  given [T] => Distributable[DAP.State[T], T] =
    new Distributable[DAP.State[T], T]:
      extension (s: DAP.State[T])
        override def msg: T = s.msg
        override def updated(msg: T): DAP.State[T] = s.copy(local = s.local union MSet(msg))
