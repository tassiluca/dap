package dap

import scala.scalanative.unsafe.*

/** Static object exposing native API, directly callable from C. */
object NativeCTMCApi extends NativeCTMCBaseApi:

  import dap.shared.modelling.CTMC.*
  import dap.shared.modelling.CTMC
  import dap.shared.modelling.CTMC.ofTransitions

  override type NativeState = Ptr[CStruct0]
  type NativeAction = CStruct2[CDouble, NativeState]
  type NativeTransition = CStruct2[NativeState, NativeAction]

  @exported("create_ctmc_from_transitions")
  def ofTransitions(transitionsPtr: Ptr[NativeTransition], size: CSize): CTMC[NativeState] =
    val transitions = (0 until size.toInt)
      .map(t => Transition(transitionsPtr(t)._1, Action(transitionsPtr(t)._2._1, transitionsPtr(t)._2._2)))
      .toSet
    CTMC.ofTransitions(transitions)

  @exported("simulate_ctmc")
  def simulateCTMC(ctmcPtr: Ptr[CTMC[NativeState]], s0: NativeState, steps: CInt): Ptr[NativeTrace] =
    simulate(ctmcPtr, s0, steps, identity, identity)
end NativeCTMCApi
