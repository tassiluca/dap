package dap

import dap.CUtils.{ freshPointer, requireNonNull }

import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.Size.intToSize
import scala.scalanative.unsafe.{ sizeOf, CDouble, CInt, CSize, CStruct2, Ptr }

/** A module exposing common native APIs functionalities for CTMC simulation. */
trait NativeCTMCBaseApi:

  type State
  type Event = CStruct2[CDouble, State]
  type Trace = CStruct2[Ptr[Event], CSize]

  import dap.shared.modelling.CTMC
  import dap.shared.modelling.CTMCSimulation.*

  def simulate[T](ctmcPtr: Ptr[CTMC[T]], s0: State, steps: CInt, f: State => T, fInv: T => State)(using
      scalanative.unsafe.Tag[State],
  ): Ptr[Trace] =
    val ctmc = !requireNonNull(ctmcPtr)
    val trace = freshPointer[Trace]()
    val events = freshPointer[Event](steps)
    ctmc
      .newSimulationTrace(f(s0), new java.util.Random)
      .take(steps)
      .zipWithIndex
      .foreach: (e, i) =>
        val event = events(i)
        event._1 = e.time
        event._2 = fInv(e.state)
    trace._1 = events
    trace._2 = steps.toCSize
    trace
  end simulate
end NativeCTMCBaseApi
