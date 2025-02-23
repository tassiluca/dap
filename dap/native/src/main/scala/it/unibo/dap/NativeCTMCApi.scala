package it.unibo.dap

import CUtils.{ freshPointer, requireNonNull }

import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.Size.intToSize
import scala.scalanative.unsafe.{ sizeOf, CDouble, CInt, CSize, CStruct2, Ptr }

/** A module exposing common native APIs functionalities for CTMC simulation. */
trait NativeCTMCApi:

  import it.unibo.dap.modelling.CTMC

  /** Native binding for state type in a CTMC process (the generic type of [[CTMC]]). */
  type State

  /** Native binding for [[CTMCSimulation.Event]] type. */
  type Event = CStruct2[CDouble, State]

  /** Native binding for the [[CTMCSimulation.Trace]] type. */
  type Trace = CStruct2[Ptr[Event], CSize]

  /** Native binding of the [[simulate]] method.
    * @param ctmcPtr the pointer to the [[CTMC]] to simulate
    * @param s0 the initial state
    * @param steps the number of steps to simulate
    * @param f the function to convert the simulation [[State]] to the CTMC state type `T`.
    *          This is used since the two types may not coincide for native interoperability reasons.
    * @param fInv the inverse function of `f`
    * @tparam T the type of the CTMC state
    * @return a pointer to the simulation trace
    */
  def simulate[T](
      ctmcPtr: Ptr[CTMC[T]],
      s0: State,
      steps: CInt,
      f: State => T,
      fInv: T => State,
  )(using scalanative.unsafe.Tag[State]): Ptr[Trace] =
    val ctmc = !requireNonNull(ctmcPtr)
    val trace = freshPointer[Trace]()
    val events = freshPointer[Event](steps)
    ctmc
      .simulate(f(s0), new java.util.Random)
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
end NativeCTMCApi
