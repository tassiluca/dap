package dap

import java.util.Random

import scala.scalanative.unsafe.*

import dap.CUtils.*

object libctmc:

  import dap.modelling.CTMC
  import CTMC.*
  import dap.modelling.CTMCSimulation.*

  private type State = Ptr[Byte]
  private type Action = CStruct2[CDouble, State]
  private type Transition = CStruct2[State, Action]
  private type Event = CStruct2[CDouble, State]
  private type Trace = CStruct2[Ptr[Event], CSize]

  @exported("create_ctmc_from_transitions")
  def ofTransitions(transitionsPtr: Ptr[Transition], size: CSize): Ptr[CTMC[State]] =
    val ptr = freshPointer[CTMC[State]]()
    val transitions = (0 until size.toInt).map: t =>
      Transition(transitionsPtr(t)._1, Action(transitionsPtr(t)._2._1, transitionsPtr(t)._2._2))
    !ptr = CTMC.ofTransitions(transitions*)
    ptr

  @exported
  def simulate(ctmcPtr: Ptr[CTMC[State]], s0: State, steps: CSize): Ptr[Trace] =
    val ctmc = !requireNonNull(ctmcPtr)
    val trace = freshPointer[Trace]()
    val events = freshPointer[Event](steps.toInt)
    val simulationResults = ctmc
      .newSimulationTrace(s0, new Random)
      .take(steps.toInt)
      .toList
    simulationResults.zipWithIndex.foreach: (e, i) =>
      val current = events(i)
      current._1 = e.time
      current._2 = e.state
    trace._1 = events
    trace._2 = steps
    trace

end libctmc
