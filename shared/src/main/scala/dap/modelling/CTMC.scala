package dap.modelling

trait CTMC[S]:
  import CTMC.Action
  def transitions(a: S): Set[Action[S]] // rate + state

object CTMC:

  case class Action[S](rate: Double, state: S)

  extension [S](rate: Double) def -->(state: S): Action[S] = Action(rate, state)

  case class Transition[S](state: S, action: Action[S])

  def ofTransitions[S](rel: Transition[S]*): CTMC[S] = ofRelation(rel.toSet)

  def ofRelation[S](rel: Set[Transition[S]]): CTMC[S] =
    ofFunction(s => rel filter (_.state == s) map (_.action))

  def ofFunction[S](f: PartialFunction[S, Set[Action[S]]]): CTMC[S] =
    s => f.applyOrElse(s, (_: S) => Set[Action[S]]())
