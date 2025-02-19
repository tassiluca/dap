package it.unibo.dap.modelling

import it.unibo.dap.modelling.CTMC.*
import it.unibo.dap.utils.MSet

/** Modules defining the concept of Distributed Asynchronous stochastic Petri net. */
object DAP:
  /** Rule of the net: `pre --rateExp--> eff | ^msg`. */
  case class Rule[T](pre: MSet[T], rateExp: MSet[T] => Double, eff: MSet[T], msg: T)

  /** Whole net's type. */
  type DAP[T] = Set[Rule[T]]

  /** State of the network at a given time, with neighboring as a map. */
  case class State[T](local: MSet[T], msg: T)

  def apply[T](rules: Rule[T]*): DAP[T] = rules.toSet

  def apply[T](rules: Set[Rule[T]]): DAP[T] = rules

  def toCTMC[T](spn: DAP[T]): CTMC[State[T]] = CTMC.ofFunction(toPartialFunction(spn))

  // Here's the implementation of operational semantics
  private def toPartialFunction[T](spn: DAP[T]): PartialFunction[State[T], Set[Action[State[T]]]] =
    case State(tokens, _) =>
      for
        Rule(pre, rateExp, eff, msg) <- spn // get any rule
        out <- tokens extract pre // checks if that node matches pre
        newtokens = out union eff // generate new tokens
        rate = rateExp(tokens) // compute rate
      yield Action(rate, State(newtokens, msg))

end DAP
