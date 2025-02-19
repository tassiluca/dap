package it.unibo.dap.modelling

trait Simulatable[M[_]]:

  export Simulatable.*
  import java.util.Random

  extension [S](self: M[S])
    def simulate(initialState: S, rnd: Random): Trace[S]
    def simulateStep(s: S, rnd: Random): Event[S] = simulate(s, rnd).drop(1).head

object Simulatable:
  /** A simulation trace as a lazy list of events. */
  type Trace[S] = LazyList[Event[S]]

  /** A simulation event, carrying the time and the state. */
  case class Event[S](time: Double, state: S)
