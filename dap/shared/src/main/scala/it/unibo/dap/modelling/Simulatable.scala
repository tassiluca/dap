package it.unibo.dap.modelling

trait Simulatable[F[_]]:

  export Simulatable.*
  import java.util.Random
  
  extension [S](self: F[S])

    def simulate(initialState: S, rnd: Random): Trace[S]

    def simulateStep(s: S, rnd: Random): Event[S] = simulate(s, rnd).head

object Simulatable:
  /** A simulation trace as a lazy list of events. */
  type Trace[A] = LazyList[Event[A]]

  /** A simulation event, carrying the time and the state. */
  case class Event[A](time: Double, state: A)
