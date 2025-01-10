package dap

import scala.scalanative.unsafe.*
import scala.scalanative.unsafe.Size.intToSize
import dap.CUtils.*
import scala.scalanative.libc.stdlib

object libctmc:

  import dap.modelling.CTMC
  import CTMC.*
  import dap.modelling.CTMCSimulation.*

  type State = Ptr[CStruct0]
  type Action = CStruct2[CDouble, State]
  type Transition = CStruct2[State, Action]
  type Event = CStruct2[CDouble, State]
  type Trace = CStruct2[Ptr[Event], CSize]

  @exported("create_ctmc_from_transitions")
  def ofTransitions(transitionsPtr: Ptr[Transition], size: CSize): CTMC[State] =
    val transitions = (0 until size.toInt).map: t =>
      Transition(transitionsPtr(t)._1, Action(transitionsPtr(t)._2._1, transitionsPtr(t)._2._2))
    CTMC.ofTransitions(transitions*)

  @exported
  def simulate(ctmcPtr: Ptr[CTMC[State]], s0: State, steps: CInt): Ptr[Trace] =
    val ctmc = !requireNonNull(ctmcPtr)
    val trace = stdlib.malloc(sizeOf[Trace]).asInstanceOf[Ptr[Trace]]
    val events = stdlib.malloc(sizeOf[Event] * steps).asInstanceOf[Ptr[Event]]
    ctmc
      .newSimulationTrace(s0, new java.util.Random)
      .take(steps)
      .zipWithIndex
      .foreach: (e, i) =>
        val event = events(i)
        event._1 = e.time
        event._2 = e.state
    trace._1 = events
    trace._2 = steps.toCSize
    trace

end libctmc
