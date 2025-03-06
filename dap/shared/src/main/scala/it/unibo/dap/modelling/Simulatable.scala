package it.unibo.dap.modelling

/** A type class for simulatable models.
  * @tparam M the type of the model to simulate
  */
trait Simulatable[M[_]]:

  export Simulatable.*
  import java.util.Random

  extension [S](self: M[S])
    /** Simulate the model from the given [[initialState]] generating a lazy [[Trace]] of [[Event]]s. */
    def simulate(initialState: S)(using Random): Trace[S]

    /** @return the next simulation [[Event]] from the given state [[s]]. */
    def simulateStep(s: S)(using Random): Event[S] = simulate(s).drop(1).head

object Simulatable:
  /** A simulation trace as a lazy list of events. */
  type Trace[S] = LazyList[Event[S]]

  /** A simulation event, carrying the time and the state. */
  case class Event[S](time: Double, state: S)
