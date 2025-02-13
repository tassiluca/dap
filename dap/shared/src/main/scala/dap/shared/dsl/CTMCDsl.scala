package dap.shared.dsl

object CTMCDsl:

  export dap.shared.modelling.CTMC
  export CTMC.*

  extension [S](state: S) def --(rate: Double): TransitionRule[S] = TransitionRule(state, rate)

  class TransitionRule[S] private[CTMCDsl](initialState: S, rate: Double):
    def -->(finalState: S): Transition[S] = Transition(initialState, Action(rate, finalState))
