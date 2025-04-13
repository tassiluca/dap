package it.unibo.dap.model

import it.unibo.dap.model.CTMC.*

/** Module defining the concept of Distributed Asynchronous stochastic Petri net. */
object DAP:

  /** Net rule guiding the evolution of the net: `pre --rateExp--> eff | ^msg`.
    * @param pre the preconditions to be met for this rule to be fired
    * @param rateExp the markovian rate of this transition
    * @param eff the effects applied to the net state when this rule is selected for execution
    * @param msg the messages sent to the node neighbours when this rule is selected for execution
    * @tparam T the token type
    */
  case class Rule[T](pre: MSet[T], rateExp: MSet[T] => Double, eff: MSet[T], msg: Option[T])

  /** Whole net's type. */
  type DAP[T] = Set[Rule[T]]

  /** State of the network at a given time. */
  case class State[T](tokens: MSet[T], msg: Option[T])

  def apply[T: Equatable](rules: Rule[T]*): DAP[T] = rules.toSet

  def apply[T: Equatable](rules: Set[Rule[T]]): DAP[T] = rules

  def toCTMC[T: Equatable](spn: DAP[T]): CTMC[State[T]] = CTMC.ofFunction(toPartialFunction(spn))

  private def toPartialFunction[T: Equatable](spn: DAP[T]): PartialFunction[State[T], Set[Action[State[T]]]] =
    case State(tokens, _) =>
      for
        Rule(pre, rateExp, eff, msg) <- spn // get any rule
        out <- tokens extract pre // checks if that node matches pre
        newtokens = out union eff // generate new tokens
        rate = rateExp(tokens) // compute rate
      yield Action(rate, State(newtokens, msg))

  given [T: Equatable] => Conversion[DAP[T], CTMC[State[T]]] = toCTMC
end DAP
