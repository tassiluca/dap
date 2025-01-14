package dap

import scala.scalanative.unsafe.*

/** Static object exposing native API for basic CTMC creation and simulation. */
object NativeCTMCApi extends NativeCTMCBaseApi:

  import dap.shared.modelling.CTMC.*
  import dap.shared.modelling.CTMC
  import dap.shared.modelling.CTMC.ofTransitions

  override type State = Ptr[CStruct0]
  type Action = CStruct2[CDouble, State]
  type Transition = CStruct2[State, Action]

  @exported("create_ctmc_from_transitions")
  def ofTransitions(transitionsPtr: Ptr[Transition], size: CSize): CTMC[State] =
    val transitions = (0 until size.toInt)
      .map(t => Transition(transitionsPtr(t)._1, Action(transitionsPtr(t)._2._1, transitionsPtr(t)._2._2)))
      .toSet
    CTMC.ofTransitions(transitions)

  @exported("simulate_ctmc")
  def simulateCTMC(ctmcPtr: Ptr[CTMC[State]], s0: State, steps: CInt): Ptr[Trace] =
    simulate(ctmcPtr, s0, steps, identity, identity)
end NativeCTMCApi
