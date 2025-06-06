package it.unibo.dap.model.dsl

object CTMCDsl:

  export it.unibo.dap.model.CTMC
  export CTMC.*

  extension [S](state: S) def --(rate: Double): (S, Double) = (state, rate)

  extension [S](self: (S, Double))
    def -->(finalState: S): Transition[S] = Transition(self._1, Action(self._2, finalState))
