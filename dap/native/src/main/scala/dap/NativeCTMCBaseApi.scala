package dap

import dap.CUtils.requireNonNull

import scala.scalanative.libc.{ stdio, stdlib }
import scala.scalanative.unsafe.Size.intToSize
import scala.scalanative.unsafe.{ sizeOf, CDouble, CInt, CQuote, CSize, CStruct2, Ptr, Zone }

trait NativeCTMCBaseApi:

  type NativeState
  type NativeEvent = CStruct2[CDouble, NativeState]
  type NativeTrace = CStruct2[Ptr[NativeEvent], CSize]

  import dap.shared.modelling.CTMC
  import dap.shared.modelling.CTMCSimulation.*

  def simulate[T](
      ctmcPtr: Ptr[CTMC[T]],
      s0: NativeState,
      steps: CInt,
      f: NativeState => T,
      fInv: T => NativeState,
  )(using scalanative.unsafe.Tag[NativeState]): Ptr[NativeTrace] =
    val ctmc = !requireNonNull(ctmcPtr)
    val trace = stdlib.malloc(sizeOf[NativeTrace]).asInstanceOf[Ptr[NativeTrace]]
    val events = stdlib.malloc(sizeOf[NativeEvent] * steps).asInstanceOf[Ptr[NativeEvent]]
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
