package dap.shared.modelling

import java.util.Random

import dap.shared.utils.Stochastics

object CTMCSimulation:

  case class Event[A](time: Double, state: A)

  type Trace[A] = LazyList[Event[A]]

  export CTMC.*

  extension [S](self: CTMC[S])

    /** @return a simulation trace of the CTMC starting from `s0`, implementing the Gillespie algorithm */
    def newSimulationTrace(s0: S, rnd: Random): Trace[S] =
      LazyList.iterate(Event(0.0, s0)) { case Event(t, s) =>
        if self.transitions(s).isEmpty
        then Event(t, s)
        else
          val choices = self.transitions(s) map (t => (t.rate, t.state))
          val next = Stochastics.cumulative(choices.toList)
          val sumR = next.last._1
          val choice = Stochastics.draw(next)(using rnd)
          Event(t + Math.log(1 / rnd.nextDouble()) / sumR, choice)
      }
end CTMCSimulation
