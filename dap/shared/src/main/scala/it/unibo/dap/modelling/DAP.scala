package it.unibo.dap.modelling

import it.unibo.dap.modelling.CTMC.*
import it.unibo.dap.utils.MSet

/** Modules defining the concept of Distributed Asynchronous stochastic Petri net. */
object DAP:
  /** Rule of the net: `pre --rateExp--> eff | ^msg`. */
  case class Rule(pre: MSet[String], rateExp: MSet[String] => Double, eff: MSet[String], msg: String)

  /** Whole net's type. */
  type DAP = Set[Rule]

  /** State of the network at a given time, with neighboring as a map. */
  case class State(local: MSet[String], msg: String)

  def apply(rules: Rule*): DAP = rules.toSet

  def apply(rules: Set[Rule]): DAP = rules

  def toCTMC(spn: DAP): CTMC[State] = CTMC.ofFunction(toPartialFunction(spn))

  // Here's the implementation of operational semantics
  private def toPartialFunction[P](spn: DAP): PartialFunction[State, Set[Action[State]]] =
    case State(tokens, _) =>
      for
        Rule(pre, rateExp, eff, msg) <- spn // get any rule
        out <- tokens extract pre // checks if that node matches pre
        newtokens = out union eff // generate new tokens
        rate = rateExp(tokens) // compute rate
      yield Action(rate, State(newtokens, msg))

end DAP
