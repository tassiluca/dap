package it.unibo.dap.modelling

/** A Continuous Time Markov Chain. */
trait CTMC[S]:
  import CTMC.Action
  def transitions(a: S): Set[Action[S]] // rate + state

object CTMC:
  /** A transition as a pair of initial state and [[Action]]. */
  case class Transition[S](state: S, action: Action[S])

  /** An action, namely a Markovian rate and a destination state. */
  case class Action[S](rate: Double, state: S)

  /** Creates a CTMC extensionally from a set of [[Transition]]. */
  def ofTransitions[S](rels: Transition[S]*): CTMC[S] = ofTransitions(rels.toSet)

  /** Creates a CTMC extensionally from a set of [[Transition]]. */
  def ofTransitions[S](rels: Set[Transition[S]]): CTMC[S] =
    ofFunction(s => rels filter (_.state == s) map (_.action))

  /** Creates a CTMC intensionally from a partial function [[f]], where
    * undefined cases are considered as no transitions.
    */
  def ofFunction[S](f: PartialFunction[S, Set[Action[S]]]): CTMC[S] =
    s => f.applyOrElse(s, (_: S) => Set[Action[S]]())

  given Simulatable[CTMC] with

    import it.unibo.dap.utils.Stochastics
    import java.util.Random

    extension [S](self: CTMC[S])

      /** Simulates the CTMC through the Gillespie algorithm. */
      override def simulate(initialState: S)(using rnd: Random): Trace[S] =
        LazyList.iterate(Event(0.0, initialState)):
          case Event(t, s) =>
            scribe.info("[Sim] before event transitions")
            if self.transitions(s).isEmpty
            then
              scribe.info("[Sim] no transitions")
              Event(t, s)
            else
              scribe.info("[Sim] before transitions")
              val choices = self.transitions(s) map (t => (t.rate, t.state))
              val next = Stochastics.cumulative(choices.toList)
              val sumR = next.last._1
              val choice = Stochastics.draw(next)
              Event(t + Math.log(1 / rnd.nextDouble()) / sumR, choice)
  end given
end CTMC
